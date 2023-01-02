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
package uk.ac.gate.cloud.cli.commands.job;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.cli.commands.DownloadingCommand;
import uk.ac.gate.cloud.client.RestClient;
import uk.ac.gate.cloud.client.RestClientException;
import uk.ac.gate.cloud.common.Downloadable;

public class Download extends DownloadingCommand {

  public void run(RestClient client, boolean jsonOutput, String... args) throws Exception {
    if(args.length < 1) {
      showHelp();
      System.exit(1);
    }

    Downloadable res = null;
    boolean overwrite = false;
    File baseDir = null;
    String outputFile = null;
    for(int j = 0; j < args.length; j++) {
      if("-f".equals(args[j])) {
        overwrite = true;
      } else if("-d".equals(args[j])) {
        if(outputFile != null) {
          System.err.println("-d and -o are mutually exclusive");
          System.exit(1);
        }
        j++;
        if(j == args.length) {
          System.err.println("-d option requires an argument");
          showHelp();
          System.exit(1);
        }
        baseDir = new File(args[j]);
      } else if("-o".equals(args[j])) {
        if(baseDir != null) {
          System.err.println("-d and -o are mutually exclusive");
          System.exit(1);
        }
        j++;
        if(j == args.length) {
          System.err.println("-o option requires an argument");
          showHelp();
          System.exit(1);
        }
        outputFile = args[j];
      } else {
        res = new Downloadable(new URL(args[j]));
      }
    }

    if(res == null) {
      System.err.println("URL is required.");
      showHelp();
      System.exit(1);
    }

    res.setClient(client);
    List<Downloaded> downloads = doDownload(Collections.singletonList(res),
            outputFile == null ? null : Collections.singletonList(outputFile),
            baseDir, jsonOutput, overwrite);
    if(jsonOutput && !downloads.isEmpty()) {
      mapper.writeValue(System.out, downloads.get(0));
    }

  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  download <url> [-f] [-d directory | -o outputfile]");
    System.err.println();
    System.err.println("Download a single file from GATE Cloud, either a single entry");
    System.err.println("in a data bundle or a single report or debug log zip file from");
    System.err.println("an annotation job.");
    System.err.println();
    System.err.println(" <url> : the URL to download, which must be one of the entry URLs from");
    System.err.println("         \"bundle-details\" or one of the URLs from \"list-reports\"");
    System.err.println(" -d directory : directory into which the file should be downloaded;");
    System.err.println("                the file is saved with the same base name as in the URL.");
    System.err.println("                If omitted, default is the current directory.  Not valid");
    System.err.println("                with \"-o\"");
    System.err.println(" -o outputfile : specify a specific path for the output file.  This can");
    System.err.println("                 be an absolute path or a file name or relative path resolved");
    System.err.println("                 against the current directory.  Not valid with \"-d\"");
    System.err.println(" -f : force, i.e. if there is already a file in the output directory");
    System.err.println("      with the desired name, overwrite it.  If omitted the newly downloaded");
    System.err.println("      file will be renamed with a numeric suffix.");
  }
}
