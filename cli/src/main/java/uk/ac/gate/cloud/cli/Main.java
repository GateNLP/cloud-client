/*
 * Copyright (c) 2016 The University of Sheffield
 *
 * This file is part of the GATE Cloud REST client library, and is
 * licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.gate.cloud.cli;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import uk.ac.gate.cloud.client.RestClient;
import uk.ac.gate.cloud.client.RestClientException;

public class Main {

  /**
   * @param args
   */
  public static void main(String... args) throws Exception {
    boolean jsonOutput = false;
    if(args.length > 0 && "--json".equals(args[0])) {
      jsonOutput = true;
      args = Arrays.copyOfRange(args, 1, args.length);
    }

    if(args.length > 0 && "configure".equals(args[0])) {
      doConfigure();
    }

    Properties commands = new Properties();
    InputStream commandsStream =
            Main.class.getResourceAsStream("commands.properties");
    commands.load(commandsStream);
    commandsStream.close();

    String command = null, commandClass = null;
    if(args.length > 0) {
      command = args[0];
      String actualCommand = command;
      commandClass = commands.getProperty(command);
      if("help".equals(command) && args.length > 1) {
        actualCommand = args[1];
        commandClass = commands.getProperty(actualCommand);
        if("configure".equals(actualCommand)) {
          System.err.println("Usage:");
          System.err.println();
          System.err.println("  configure");
          System.err.println();
          System.err.println("Interactively configure this tool with your GATE Cloud API");
          System.err.println("credentials.");
          System.exit(0);
        }
      }
      if(commandClass == null) {
        System.err.println("Unknown command \"" + actualCommand + "\"");
      }
    }

    if(commandClass == null) {
      // either no command, just "help" with no arg, or "help cmd" for invalid cmd
      System.err.println("Usage:");
      System.err.println();
      System.err.println("    java -jar gate-cloud-cli.jar [--json] <command> [options]");
      System.err.println();
      System.err.println("  --json - produce output as JSON instead of the default human-readable");
      System.err.println("           tabular format");
      System.err.println();
      System.err.println("Valid commands are:");
      List<String> validCommands = new ArrayList<String>();
      validCommands.add("configure");
      validCommands.addAll(commands.stringPropertyNames());
      Collections.sort(validCommands);
      for(String cmd : validCommands) {
        System.err.println("  " + cmd);
      }
      System.err.println();
      System.err.println("For details of the parameters for a particular command, use");
      System.err.println("\"help <command>\", or see the wiki.");
      System.exit(1);
    }

    Command cmd =
            Class.forName(commandClass)
                    .asSubclass(Command.class).getDeclaredConstructor().newInstance();
    if("help".equals(command)) {
      cmd.showHelp();
      System.exit(0);
    }

    String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);
    try {
      RestClient client = createClient();
      cmd.run(client, jsonOutput, cmdArgs);
    } catch(RestClientException e) {
      String response =
              (e.getResponse() == null) ? "(no detail available)" : e
                      .getResponse().at("/message").asText();
      System.err.println(e.getMessage() + ": " + response);
    }
  }

  private static void doConfigure() {
    File configFile = findConfigFile();
    Console console = System.console();
    if(console == null) {
      System.err.println("Could not get java.io.Console - create configuration manually.");
      configFileUsage(configFile);
    }
    System.out.println("Configuration");
    System.out.println("-------------");
    System.out.println();
    System.out.println("This client requires an API Key to authenticate to the GATE Cloud");
    System.out.println("APIs.  You can generate one from your account settings page on");
    System.out.println("https://cloud.gate.ac.uk - ensure you enable the appropriate");
    System.out.println("permissions for the API actions you intend to call.");
    System.out.println();
    String apiKeyId = console.readLine("API key id: ");
    char[] password = console.readPassword("API key password: ");
    System.out.println("Writing configuration to " + configFile.getAbsolutePath());
    Properties config = new Properties();
    if(configFile.canRead()) {
      try {
        FileInputStream confIn = FileUtils.openInputStream(configFile);
        try {
          config.load(confIn);
        } finally {
          IOUtils.closeQuietly(confIn);
        }
      } catch(IOException e) {
        // ignore for the moment
      }
    }
    config.setProperty("keyId", apiKeyId);
    config.setProperty("password", new String(password));
    File newConfigFile = new File(configFile.getAbsolutePath() + ".new");
    try {
      FileOutputStream out = FileUtils.openOutputStream(newConfigFile);
      try {
        config.store(out, "Generated by GATE Cloud command line tools");
      } finally {
        IOUtils.closeQuietly(out);
      }
      configFile.delete();
      FileUtils.moveFile(newConfigFile, configFile);
      System.out.println("Configuration saved successfully.");
      System.exit(0);
    } catch(IOException e) {
      System.err.println("Could not write config file - please configure manually.");
      configFileUsage(configFile);
    }
  }

  private static RestClient createClient() throws Exception {
    File configFile = findConfigFile();

    Properties config = new Properties();
    if(configFile.canRead()) {
      try {
        FileInputStream confIn = FileUtils.openInputStream(configFile);
        try {
          config.load(confIn);
        } finally {
          IOUtils.closeQuietly(confIn);
        }
      } catch(IOException e) {
        // ignore for the moment
      }
    }

    String keyId =
            System.getProperty("gate.cloud.apiKey.id",
                    config.getProperty("keyId"));
    String password =
            System.getProperty("gate.cloud.apiKey.password",
                    config.getProperty("password"));
    String baseUrl =
            System.getProperty(
                    "gate.cloud.baseUrl",
                    config.getProperty("baseUrl",
                            RestClient.DEFAULT_BASE_URL.toString()));

    if(keyId == null || password == null) {
      System.err.println("API key not found - please run the \"configure\" command to create");
      System.err.println("a configuration file.");
      System.exit(1);
    }

    return new RestClient(new URL(baseUrl), keyId, password);
  }

  private static void configFileUsage(File configFile) {
    System.err
            .println("Please provide your API key and password by creating a configuration file:");
    System.err.println();
    System.err.println(configFile.getAbsolutePath());
    System.err.println();
    System.err.println("containing the two lines:");
    System.err.println();
    System.err.println("keyId = <API key ID>");
    System.err.println("password = <password>");
    System.exit(1);
  }

  private static File findConfigFile() {
    File configFile;
    File userHome = new File(System.getProperty("user.home"));
    String osName = System.getProperty("os.name", "unknown").toLowerCase();
    if(osName.indexOf("mac os") >= 0) {
      configFile =
              new File(userHome,
                      "Library/Application Support/cloud.gate.ac.uk/client.conf");
    } else if(osName.indexOf("window") >= 0) {
      configFile =
              new File(System.getenv("APPDATA"), "cloud.gate.ac.uk/client.conf");
    } else {
      configFile = new File(userHome, ".gate-cloud-client.conf");
    }
    return configFile;
  }

}
