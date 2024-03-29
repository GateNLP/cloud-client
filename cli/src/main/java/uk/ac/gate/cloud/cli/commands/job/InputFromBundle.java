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

import uk.ac.gate.cloud.job.InputDetails;
import uk.ac.gate.cloud.job.Job;
import uk.ac.gate.cloud.job.JobManager;

public class InputFromBundle extends AbstractCommand {

  @Override
  public void run(RestClient client, boolean jsonOutput, String... args) throws Exception {
    if(args.length < 2) {
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
    
    long bundleId = -1;
    try {
      bundleId = Long.parseLong(args[1]);
    } catch(NumberFormatException e) {
      System.err.println("Bundle ID must be a valid number");
      System.exit(1);
    }
    
    if(!jsonOutput) {
      System.out.println("Configuring job " + jobId + " to take input from bundle " + bundleId);
    }

    JobManager mgr = new JobManager(client);
    Job job = mgr.getJob(jobId);
    InputDetails input = job.addBundleInput(bundleId);
    if(jsonOutput) {
      mapper.writeValue(System.out, input);
    } else {
      System.out.println("Created " + input.url);
    }
  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  input-from-bundle <jobid> <bundleId>");
    System.err.println();
    System.err.println("Configure a job to take input from a data bundle.");
    System.err.println(" - jobid : numeric ID of the job");
    System.err.println(" - bundleId : numeric ID of the data bundle");
  }
}
