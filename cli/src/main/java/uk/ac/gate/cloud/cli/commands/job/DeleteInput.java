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
import uk.ac.gate.cloud.job.JobManager;

public class DeleteInput extends AbstractCommand {

  public void run(RestClient client, boolean jsonOutput, String... args) throws Exception {
    if(args.length < 1) {
      showHelp();
      System.exit(1);
    }

    JobManager mgr = new JobManager(client);
    InputDetails input = mgr.getInputDetails(args[0]);
    input.delete();
    if(jsonOutput) {
      System.out.println("{\"success\":true}");
    } else {
      System.out.println("Input deleted successfully");
    }
  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  delete-input <inputurl>");
    System.err.println();
    System.err.println("Delete the given input from its job.  The inputurl should be");
    System.err.println("a \"detail URL\" returned from list-inputs.");
  }

}
