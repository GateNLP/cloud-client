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
package uk.ac.gate.cloud.cli.commands;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Formatter;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.ac.gate.cloud.cli.Command;

import uk.ac.gate.cloud.client.RestClient;
import uk.ac.gate.cloud.common.Prices;

public abstract class AbstractCommand implements Command {

  protected NumberFormat currencyFormatter;
  protected NumberFormat percentFormatter;
  protected ObjectMapper mapper;

  public AbstractCommand() {
    currencyFormatter = NumberFormat.getCurrencyInstance();
    currencyFormatter.setCurrency(Currency.getInstance("GBP"));
    percentFormatter = NumberFormat.getPercentInstance();
    percentFormatter.setMaximumFractionDigits(2);
    mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
  }

  @Override
  public void run(RestClient client, boolean jsonOutput, String... args) throws Exception {
    run(client, args);
  }

  @Override
  public void run(RestClient client, String... args) throws Exception {
    throw new UnsupportedOperationException();
  }

  public String formatPrices(Prices p) {
    StringBuilder buf = new StringBuilder();
    if(p.setup > 0) {
      buf.append(currencyFormatter.format(p.setup));
      if(p.hour > 0 || p.mib > 0) {
        buf.append(", plus ");
      }
    }
    if(p.hour > 0) {
      buf.append(currencyFormatter.format(p.hour));
      buf.append(" per hour");
      if(p.mib > 0) {
        buf.append(", and ");
      }
    }
    if(p.mib > 0) {
      buf.append(currencyFormatter.format(p.mib));
      buf.append(" per MiB");
    }
    return buf.toString();
  }
  
  public String formatMs(long millis) {
    StringBuilder buf = new StringBuilder();
    Formatter fmt = new Formatter(buf);
    boolean started = false;
    if(millis >= 24 * 60 * 60 * 1000) { // 1 day
      started = true;
      long days = millis / (24 * 60 * 60 * 1000);
      buf.append(days + ":");
      millis = millis % (24 * 60 * 60 * 1000);
    }
    if(started || millis >= 60 * 60 * 1000) { // 1 hour
      started = true;
      long hours = millis / (60 * 60 * 1000);
      fmt.format("%02d:", hours);
      millis = millis % (60 * 60 * 1000);
    }
    if(started || millis >= 60 * 1000) { // 1 minute
      started = true;
      long mins = millis / (60 * 1000);
      fmt.format("%02d:", mins);
      millis = millis % (60 * 1000);
    }
    long secs = millis / (1000);
    millis = millis % 1000;
    fmt.format("%02d.%03d", secs, millis);
    fmt.close();
    return buf.toString();
  }

  public String formatBytes(long bytes) {
    if(bytes >= 1024 * 1024 * 1024) {
      return (bytes / (1024 * 1024 * 1024)) + "GiB";
    } else if(bytes >= 1024 * 1024) {
      return (bytes / (1024 * 1024)) + "MiB";
    } else if(bytes >= 1024) {
      return (bytes / (1024)) + "KiB";
    } else {
      return bytes + "B";
    }
  }
  
  public String formatPercent(double val) {
    return percentFormatter.format(val);
  }

  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  " + this.getClass().getSimpleName().replaceAll(
            "(?<=[a-z])([A-Z])", "-$1").toLowerCase(Locale.ENGLISH));
  }
}
