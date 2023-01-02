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

import java.util.*;

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;

import uk.ac.gate.cloud.job.Job;
import uk.ac.gate.cloud.job.JobManager;
import uk.ac.gate.cloud.job.JobState;
import uk.ac.gate.cloud.job.JobSummary;

public class ListJobs extends AbstractCommand {

  public void run(RestClient client, boolean jsonOutput, String... args) throws Exception {
    JobManager mgr = new JobManager(client);
    JobState[] states = new JobState[args.length];
    for(int i = 0; i < args.length; i++) {
      try {
        states[i] = JobState.valueOf(args[i].toUpperCase(Locale.ENGLISH));
      } catch(IllegalArgumentException e) {
        System.err.println("Invalid job state " + args[i]);
        showHelp();
        System.exit(1);
      }
    }
    List<JobSummary> jobs = mgr.listJobs(states);
    if(jsonOutput) {
      List<Job> jobDetails = new ArrayList<>();
      if(jobs != null) {
        for(JobSummary summ : jobs) {
          jobDetails.add(summ.details());
        }
      }
      mapper.writeValue(System.out, jobDetails);
    } else {
      if(jobs == null || jobs.isEmpty()) {
        System.out.println("No jobs found");
      } else {
        // ID (6 cols), Name (32 cols), price (rest)
        System.out.println("    ID  Name                                      State");
        System.out.println("----------------------------------------------------------");
        Formatter f = new Formatter(System.out);
        for(JobSummary summ : jobs) {
          Job j = summ.details();
          String name = j.name;
          if(name.length() > 40) {
            name = name.substring(0, 37) + "...";
          }
          f.format("%6d  %-40s  %s%n", j.id, name, j.state);
        }
        f.close();
      }
    }
  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  list-jobs [state1 state2 ...]");
    System.err.println();
    System.err.println("List all your annotation jobs, optionally filtered by one");
    System.err.println("or more states:");
    System.err.println(" - RESERVED - not yet fully configured");
    System.err.println(" - READY - configured and ready to be started");
    System.err.println(" - ACTIVE - up and running");
    System.err.println(" - COMPLETED - finished running");
    System.err.println(" - SUSPENDED - paused due to insufficient funds");
    System.err.println(" - DELETED - deleted and can no longer be run");
  }

}
