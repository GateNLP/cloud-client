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

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;

import uk.ac.gate.cloud.job.Job;
import uk.ac.gate.cloud.job.JobManager;
import uk.ac.gate.cloud.job.LogMessage;

public class ExecutionLog extends AbstractCommand {
  
  private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);

  public void run(RestClient client, boolean jsonOutput, String... args) throws Exception {
    if(args.length < 1) {
      System.exit(1);
    }
    long jobId = -1;
    try {
      jobId = Long.parseLong(args[0]);
    } catch(NumberFormatException e) {
      System.err.println("Job ID must be a valid number");
      System.exit(1);
    }

    JsonGenerator gen = null;
    if(jsonOutput) {
      if(args.length > 1 && "-watch".equals(args[1])) {
        // don't wrap lines in ndJSON mode
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
      }
      gen = mapper.createGenerator(System.out)
              .setRootValueSeparator(new SerializedString("\n"))
              .enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)
              .enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    }

    JobManager mgr = new JobManager(client);
    Job job = mgr.getJob(jobId);
    Calendar lastPoll = Calendar.getInstance();
    List<LogMessage> messages = job.executionLog(null, null);
    if(args.length > 1 && "-watch".equals(args[1])) {
      printMessages(messages, gen);
      while(true) {
        Thread.sleep(15000);
        messages = job.executionLog(lastPoll, null);
        lastPoll = Calendar.getInstance();
        printMessages(messages, gen);
      }
    } else {
      if(jsonOutput) {
        // one shot mode, so print as a well-formed array
        gen.writeStartArray();
      }
      printMessages(messages, gen);
    }

    if(gen != null) {
      gen.close();
    }
  }

  private void printMessages(List<LogMessage> messages, JsonGenerator gen) throws IOException {
    // API returns the most recent message first
    Collections.reverse(messages);
    for(LogMessage m : messages) {
      if(gen == null) {
        System.out.println(dateFormat.format(m.date.getTime()) + ": " + m.message);
      } else {
        mapper.writeValue(gen, m);
      }
    }
    if(gen != null) {
      gen.flush();
    }
  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  execution-log <jobid> [-watch]");
    System.err.println();
    System.err.println("Fetches all execution log messages for the job.  If -watch");
    System.err.println("is specified, poll continuously for new messages (use CTRL-C");
    System.err.println("to stop).  For this command, JSON output mode produces a");
    System.err.println("well formed array of messages in \"one-shot\" mode (without");
    System.err.println("-watch), but in -watch mode it prints newline-delimited JSON");
    System.err.println("with one JSON message object per line in a continuous stream.");
  }
}
