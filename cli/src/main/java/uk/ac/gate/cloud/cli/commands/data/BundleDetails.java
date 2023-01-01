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
import uk.ac.gate.cloud.common.Downloadable;
import uk.ac.gate.cloud.data.DataBundle;
import uk.ac.gate.cloud.data.DataManager;

public class BundleDetails extends AbstractCommand {

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
    if(jsonOutput) {
      if("-nofiles".equals(args[1])) {
        b.files = null;
      }
      mapper.writeValue(System.out, b);
    } else {
      renderBundle(b, args.length > 1 && "-nofiles".equals(args[1]));
    }
  }

  private void renderBundle(DataBundle b, boolean noFiles) {
    System.out.println("                    ID: " + b.id);
    System.out.println("                  Name: " + b.name);
    System.out.println("          Date created: " + b.dateCreated);
    System.out.println("       Total data size: " + formatBytes(b.totalSize));
    System.out.println("        Cost per month: "
            + currencyFormatter.format(b.monthlyPrice));
    if(b.type != null) {
      System.out.println("           Bundle type: " + b.type);
      if(b.fileExtensions != null) {
        System.out.println("File extension filters: " + b.fileExtensions);
      }
      if(b.mimeTypeFilters != null) {
        System.out.println("     MIME type filters: " + b.mimeTypeFilters);
      }
      if(b.mimeTypeOverride != null) {
        System.out.println("    MIME type override: " + b.mimeTypeOverride);
      }
      if(b.encoding != null) {
        System.out.println("     Encoding override: " + b.encoding);
      }
    }
    System.out.println();
    if(b.closed) {
      if(b.downloadable) {
        System.out.println("Bundle contains " + b.files.size() + " file(s):");
        if(!noFiles) {
          for(Downloadable entry : b.files) {
            System.out.println(entry.url);
          }
        }
      } else {
        System.out
                .println("Bundle is not directly downloadable, but can be used as input");
        System.out.println("to an annotation job.");
      }
    } else {
      System.out.println("Bundle is currently open for uploads.");
    }
  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  bundle-details <bundleid or url> [-nofiles]");
    System.err.println();
    System.err.println("Show details of a bundle.  Optional switches:");
    System.err.println("  -nofiles : omit the full listing of files in this bundle.");

  }
}
