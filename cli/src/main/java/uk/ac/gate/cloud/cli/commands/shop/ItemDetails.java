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

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;

import uk.ac.gate.cloud.shop.Item;
import uk.ac.gate.cloud.shop.Shop;

public class ItemDetails extends AbstractCommand {

  public void run(RestClient client, String... args) throws Exception {
    if(args.length < 1) {
      System.err.println("Usage: item-details <itemid>");
      System.exit(1);
    }
    long itemId = -1;
    try {
      itemId = Long.parseLong(args[0]);
    } catch(NumberFormatException e) {
      System.err.println("Item ID must be a valid number");
      System.exit(1);
    }
    Shop shop = new Shop(client);
    Item i = shop.getItem(itemId);
    renderItem(i);
  }

  private void renderItem(Item i) {
    System.out.println("                 ID: " + i.id);
    System.out.println("               Name: " + i.name);
    System.out.println("              Price: " + formatPrices(i.price));
    System.out.println("Online API Endpoint: " + i.onlineUrl);
    System.out.println();
    System.out.println("Description");
    System.out.println("-----------");
    System.out.println();
    System.out.println(i.shortDescription);
  }
  
  

}
