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

import java.util.Formatter;
import java.util.List;

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;

import uk.ac.gate.cloud.data.DataBundleSummary;
import uk.ac.gate.cloud.data.DataManager;

public class ListBundles extends AbstractCommand {

  public void run(RestClient client, String... args) throws Exception {
    DataManager mgr = new DataManager(client);
    List<DataBundleSummary> bundles = mgr.listBundles();
    if(bundles == null || bundles.isEmpty()) {
      System.out.println("No data bundles found");
    } else {
      // ID (6 cols), Name (40 cols), state (rest)
      System.out.println("    ID  Name                                      Notes");
      System.out.println("----------------------------------------------------------");
      Formatter f = new Formatter(System.out);
      for(DataBundleSummary summ : bundles) {
        String name = summ.name;
        if(name.length() > 40) {
          name = name.substring(0,37) + "...";
        }
        String notes = "";
        if(!summ.closed) {
          notes = "Open for uploads";
        } else if(!summ.downloadable) {
          notes = "Not directly downloadable";
        }
        f.format("%6d  %-40s  %s%n", summ.id, name, notes);
      }
      f.close();
    }
  }

}
