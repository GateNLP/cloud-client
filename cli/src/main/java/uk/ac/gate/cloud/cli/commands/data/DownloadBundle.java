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
package uk.ac.gate.cloud.cli.commands.data;

import java.io.File;
import java.util.*;

import uk.ac.gate.cloud.cli.commands.DownloadingCommand;
import uk.ac.gate.cloud.client.RestClient;
import uk.ac.gate.cloud.data.DataBundle;
import uk.ac.gate.cloud.data.DataManager;

public class DownloadBundle extends DownloadingCommand {

  public void run(RestClient client, boolean jsonOutput, String... args) throws Exception {
    if(args.length < 1) {
      showHelp();
      System.exit(1);
    }
    DataManager mgr = new DataManager(client);
    DataBundle b = null;
    boolean overwrite = false;
    File baseDir = null;
    for(int j = 0; j < args.length; j++) {
      if("-f".equals(args[j])) {
        overwrite = true;
      } else if("-d".equals(args[j])) {
        j++;
        if(j == args.length) {
          System.err.println("-d option requires an argument");
          showHelp();
          System.exit(1);
        }
        baseDir = new File(args[j]);
      } else {
        try {
          b = mgr.getBundle(Long.parseLong(args[j]));
        } catch(NumberFormatException e) {
          // assume it's a URL
          b = mgr.getBundle(args[0]);
        }
      }
    }

    if(b == null) {
      System.err
              .println("Please specify either a numeric bundle ID or a valid bundle URL.");
      return;
    }

    List<Downloaded> downloaded = doDownload(b.files, null, baseDir, jsonOutput, overwrite);
    if(jsonOutput) {
      mapper.writeValue(System.out, downloaded);
    }
  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  download-bundle <bundleid or url> [-d directory] [-f]");
    System.err.println();
    System.err.println("Download all the files from a data bundle into local files with");
    System.err.println("the corresponding names.  To download individual files use the");
    System.err.println("\"download\" command.");
    System.err.println();
    System.err.println(" <bundleid or url> : the id of the bundle, or \"detailUrl\" from API");
    System.err.println(" -d directory : directory into which the files should be downloaded.");
    System.err.println("                If omitted, default is the current directory.");
    System.err.println(" -f : force, i.e. if there is already a file in the output directory");
    System.err.println("      with the same name as a file to download, overwrite it.  If omitted");
    System.err.println("      the newly downloaded file will be renamed with a numeric suffix.");
  }

}
