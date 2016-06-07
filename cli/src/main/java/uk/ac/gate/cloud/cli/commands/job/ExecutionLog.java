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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;

import uk.ac.gate.cloud.job.Job;
import uk.ac.gate.cloud.job.JobManager;
import uk.ac.gate.cloud.job.LogMessage;

public class ExecutionLog extends AbstractCommand {
  
  private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);

  public void run(RestClient client, String... args) throws Exception {
    if(args.length < 1) {
      System.err.println("Usage: execution-log <jobid> [-watch]");
      System.err.println("Fetches all execution log messages for the job.  If -watch");
      System.err.println("is specified, poll continuously for new messages (use CTRL-C");
      System.err.println("to stop)");
      System.exit(1);
    }
    long jobId = -1;
    try {
      jobId = Long.parseLong(args[0]);
    } catch(NumberFormatException e) {
      System.err.println("Job ID must be a valid number");
      System.exit(1);
    }

    JobManager mgr = new JobManager(client);
    Job job = mgr.getJob(jobId);
    Calendar lastPoll = Calendar.getInstance();
    List<LogMessage> messages = job.executionLog(null, null);
    printMessages(messages);
    if(args.length > 1 && "-watch".equals(args[1])) {
      while(true) {
        Thread.sleep(15000);
        messages = job.executionLog(lastPoll, null);
        lastPoll = Calendar.getInstance();
        printMessages(messages);
      }
    }
    
  }
  
  private void printMessages(List<LogMessage> messages) {
    // API returns the most recent message first
    Collections.reverse(messages);
    for(LogMessage m : messages) {
      System.out.println(dateFormat.format(m.date.getTime()) + ": " + m.message);
    }
  }

}
