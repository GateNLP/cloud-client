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

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;

import uk.ac.gate.cloud.job.Job;
import uk.ac.gate.cloud.job.JobManager;
import uk.ac.gate.cloud.job.JobState;

public class JobDetails extends AbstractCommand {

  public void run(RestClient client, boolean jsonOutput, String... args) throws Exception {
    if(args.length < 1) {
      showHelp();
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
    Job j = mgr.getJob(jobId);
    if(jsonOutput) {
      mapper.writeValue(System.out, j);
    } else {
      renderJob(j);
    }
  }

  private void renderJob(Job j) {
    System.out.println("             ID: " + j.id);
    System.out.println("           Name: " + j.name);
    System.out.println("          State: " + j.state);
    System.out.println("          Price: " + formatPrices(j.price));
    System.out.println("   Date created: " + j.dateCreated);
    if(j.state == JobState.COMPLETED) {
      System.out.println(" Date completed: " + j.dateCompleted);
      System.out.println("    Expiry date: " + j.resultsAvailableUntil);
      if(j.resultBundle != null) {
        System.out.println("  Result bundle: " + j.resultBundle);
      }
    }
    if(j.timeUsed > 0) {
      System.out.println("Processing time: " + formatMs(j.timeUsed)
              + " (charged " + formatMs(j.timeCharged) + " so far)");
    }
    if(j.bytesUsed > 0) {
      System.out.println(" Data processed: " + formatBytes(j.bytesUsed)
              + " (charged " + formatBytes(j.bytesCharged) + " so far)");
    }
    if(j.progress > 0) {
      System.out.println("   Job progress: " + formatPercent(j.progress));
    }

  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  job-details <jobid>");
    System.err.println();
    System.err.println("Display details of the job with the specified ID, which");
    System.err.println("must be an integer as returned by list-jobs.");
  }

}
