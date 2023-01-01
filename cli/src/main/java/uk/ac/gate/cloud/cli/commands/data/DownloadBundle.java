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
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;
import uk.ac.gate.cloud.client.RestClientException;
import uk.ac.gate.cloud.common.Downloadable;
import uk.ac.gate.cloud.data.DataBundle;
import uk.ac.gate.cloud.data.DataManager;

public class DownloadBundle extends AbstractCommand {

  public void run(RestClient client, boolean jsonOutput, String... args) throws Exception {
    if(args.length < 1) {
      showHelp();
      System.exit(1);
    }
    DataManager mgr = new DataManager(client);
    DataBundle b = null;
    try {
      b = mgr.getBundle(Long.parseLong(args[0]));
    } catch(NumberFormatException e) {
      // assume it's a URL
      b = mgr.getBundle(args[0]);
    }
    if(b == null) {
      System.err
              .println("Please specify either a numeric bundle ID or a valid bundle URL.");
      return;
    }

    int j = 1;
    boolean overwrite = false;
    File baseDir = null;
    while(args.length > j && args[j].startsWith("-")) {
      if("-o".equals(args[j])) {
        overwrite = true;
      } else if("-d".equals(args[j])) {
        j++;
        baseDir = new File(args[j]);
      }
      j++;
    }

    List<Downloadable> entries = b.files;
    if(args.length > j) {
      // specific files
      List<Downloadable> filteredEntries = new ArrayList<>();
      // index the list of entries by their URL and by the file name at the end of the path
      Map<String, Downloadable> entriesByName = new HashMap<>();
      for(Downloadable ent : entries) {
        entriesByName.put(ent.url.toString(), ent);
        String path = ent.url.getPath();
        path = path.substring(path.lastIndexOf('/') + 1);
        entriesByName.put(path, ent);
      }
      // find the matching downloadables for specified params
      for(int k = j; k < args.length; k++) {
        Downloadable ent = entriesByName.get(args[k]);
        if(ent == null) {
          System.err.println("Warning: " + args[k] + " is not a valid entry in this bundle");
        } else {
          filteredEntries.add(ent);
        }
      }

      entries = filteredEntries;
    }
    int i = 1;
    List<Downloaded> downloaded = new ArrayList<>();
    for(Downloadable ent : entries) {
      String path = ent.url.getPath();
      String filename = path.substring(path.lastIndexOf("/") + 1);
      File f = new File(baseDir, filename);
      if(!overwrite && f.exists()) {
        int suffix = 1;
        String baseName = filename.endsWith(".gz") ? filename.substring(0, filename.length() - 3) : filename;
        int lastDot = baseName.lastIndexOf('.');
        if(lastDot >= 0) {
          baseName = baseName.substring(0, lastDot);
        } else {
          lastDot = baseName.length();
        }
        String extension = filename.substring(lastDot);
        while(f.exists()) {
          f = new File(baseDir, baseName + "(" + suffix + ")" + extension);
          suffix++;
        }
      }
      if(jsonOutput) {
        downloaded.add(new Downloaded(filename, f.getPath()));
      } else {
        System.out.println("[" + i++ + "/" + entries.size() + "] " + filename + " -> " + f.getPath());
      }
      try {
        FileUtils.copyURLToFile(ent.urlToDownload(), f);
      } catch(IOException e) {
        throw new RestClientException("Error downloading " + ent.url);
      }
    }
    if(jsonOutput) {
      mapper.writeValue(System.out, downloaded);
    }
  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  download-bundle <bundleid or url> [-d directory] [-o] [file1 file2 ...]");
    System.err.println();
    System.err.println("Download one or more files from a data bundle into local files with.");
    System.err.println("the corresponding names.");
    System.err.println();
    System.err.println(" <bundleid or url> : the id of the bundle, or \"detailUrl\" from API");
    System.err.println(" -d directory : directory into which the files should be downloaded.");
    System.err.println("                If omitted, default is the current directory.");
    System.err.println(" -o : overwrite, i.e. if there is already a file in the output directory");
    System.err.println("      with the same name as a file to download, overwrite it.  If omitted");
    System.err.println("      the newly downloaded file will be renamed with a numeric suffix.");
    System.err.println(" file1 ... : file names or URLs to download from within the bundle.");
    System.err.println("             If omitted, all bundle files will be downloaded.");
  }

  static class Downloaded {
    public final String name;
    public final String destination;

    Downloaded(String name, String destination) {
      this.name = name;
      this.destination = destination;
    }
  }
}
