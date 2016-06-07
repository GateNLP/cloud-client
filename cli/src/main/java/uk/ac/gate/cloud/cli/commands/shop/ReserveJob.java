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
package uk.ac.gate.cloud.cli.commands.shop;

import java.net.MalformedURLException;
import java.net.URL;

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;

import uk.ac.gate.cloud.job.Job;
import uk.ac.gate.cloud.shop.Item;
import uk.ac.gate.cloud.shop.Shop;

public class ReserveJob extends AbstractCommand {

  public void run(RestClient client, String... args) throws Exception {
    if(args.length < 1) {
      System.err.println("Usage: reserve-job <itemid or URL> [\"Job name\"]");
      System.exit(1);
    }
    long itemId = -1;
    String url = null;
    try {
      itemId = Long.parseLong(args[0]);
    } catch(NumberFormatException e) {
      try {
        url = new URL(args[0]).toExternalForm();
      } catch(MalformedURLException mue) {
        System.err.println("Item argument must be a valid number (item ID) or URL");
        System.exit(1);
      }
    }
    Shop shop = new Shop(client);
    Item i = null;
    if(url == null) {
      i = shop.getItem(itemId);
    } else {
      i = shop.getItem(url);
    }
    Job job = i.reserve(Boolean.getBoolean("annomarket.payment.allowed"));
    System.out.println("Successfully reserved job.");
    // rename if a name was specified
    if(args.length >= 2) {
      job.rename(args[1]);
    }
    System.out.println();
    System.out.println("  ID: " + job.id);
    System.out.println("Name: " + job.name);
  }

}
