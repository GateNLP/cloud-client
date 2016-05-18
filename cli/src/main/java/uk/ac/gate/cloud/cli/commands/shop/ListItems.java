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

import java.util.Formatter;
import java.util.List;

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;

import uk.ac.gate.cloud.shop.Item;
import uk.ac.gate.cloud.shop.Shop;

public class ListItems extends AbstractCommand {
  
  public void run(RestClient client, String... args) throws Exception {
    Shop shop = new Shop(client);
    List<Item> items = shop.listItems(args);
    if(items == null || items.isEmpty()) {
      System.out.println("No items found");
    } else {
      // ID (6 cols), Name (42 cols), price (rest)
      System.out.println("    ID  Name                              Price");
      System.out.println("----------------------------------------------------------");
      Formatter f = new Formatter(System.out);
      for(Item i : items) {
        String name = i.name;
        if(name.length() > 42) {
          name = name.substring(0,39) + "...";
        }
        f.format("%6d  %-42s  %s%n", i.id, name, formatPrices(i.price));
      }
      f.close();
    }
  }

}
