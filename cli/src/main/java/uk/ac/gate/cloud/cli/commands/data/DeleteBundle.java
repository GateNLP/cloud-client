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

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;

import uk.ac.gate.cloud.data.DataBundle;
import uk.ac.gate.cloud.data.DataManager;

public class DeleteBundle extends AbstractCommand {

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
    } else {
      b.delete();
      if(jsonOutput) {
        System.out.println("{\"success\":true}");
      } else {
        System.out.println("Bundle " + b.id + " deleted");
      }
    }
  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  delete-bundle <bundleid or url>");
    System.err.println();
    System.err.println("Delete the data bundle with the given ID (or \"detailUrl\"");
    System.err.println("from a previous API call).  This will delete any files stored");
    System.err.println("by GATE Cloud for this bundle.");
  }
}
