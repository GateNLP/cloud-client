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

public class GetInputDetails extends AbstractCommand {

  public void run(RestClient client, String... args) throws Exception {
    if(args.length < 1) {
      System.err.println("Usage: input-details <inputurl>");
      System.exit(1);
    }

    JobManager mgr = new JobManager(client);
    InputDetails input = mgr.getInputDetails(args[0]);
    renderInput(input);
  }

  private void renderInput(InputDetails input) {
    System.out.println("               URL: " + input.url);
    if(input.sourceBundle != null) {
      // this is a data bundle input
      System.out.println();
      System.out.println("This output is derived from a data bundle, for details use");
      System.out.println();
      System.out.println("  bundle-details " + input.sourceBundle);
      System.out.println();
    }
    System.out.println("              Type: " + input.type);
    if(input.encoding != null) {
      System.out.println("          Encoding: " + input.encoding);
    }
    if(input.mimeTypeOverride != null) {
      System.out.println("MIME type override: " + input.mimeTypeOverride);
    }
    if(input.fileExtensions != null) {
      System.out.println("   File extensions: " + input.fileExtensions);
    }
    if(input.mimeTypes != null) {
      System.out.println(" MIME type filters: " + input.mimeTypes);
    }
  }

}
