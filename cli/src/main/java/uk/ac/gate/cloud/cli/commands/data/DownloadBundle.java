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
import java.util.List;

import org.apache.commons.io.FileUtils;

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;
import uk.ac.gate.cloud.client.RestClientException;
import uk.ac.gate.cloud.common.Downloadable;
import uk.ac.gate.cloud.data.DataBundle;
import uk.ac.gate.cloud.data.DataManager;

public class DownloadBundle extends AbstractCommand {

  public void run(RestClient client, String... args) throws Exception {
    if(args.length < 1) {
      System.err.println("Usage: download-bundle <bundleid or url>");
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
      System.out
              .println("Please specify either a numeric bundle ID or a valid bundle URL.");
    }

    List<Downloadable> entries = b.files;
    int i = 1;
    for(Downloadable ent : entries) {
      String path = ent.url.getPath();
      String filename = path.substring(path.lastIndexOf("/") + 1);
      System.out.println(filename + " (file " + i++ + " of " + entries.size()
              + ")");
      File f = new File(filename);
      try {
        FileUtils.copyURLToFile(ent.urlToDownload(), f);
      } catch(IOException e) {
        throw new RestClientException("Error downloading " + ent.url);
      }
    }
  }

}
