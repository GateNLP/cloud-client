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

import java.io.File;
import java.util.List;

import uk.ac.gate.cloud.cli.commands.DownloadingCommand;
import uk.ac.gate.cloud.client.RestClient;
import uk.ac.gate.cloud.job.Job;
import uk.ac.gate.cloud.job.JobManager;

public class DownloadAllReports extends DownloadingCommand {

  @Override
  public void run(RestClient client, boolean jsonOutput, String... args) throws Exception {
    if(args.length < 1) {
      showHelp();
      System.exit(1);
    }

    JobManager mgr = new JobManager(client);
    Job job = null;
    boolean overwrite = false;
    File baseDir = null;
    for(int j = 0; j < args.length; j++) {
      if("-f".equals(args[j])) {
        overwrite = true;
      } else if("-d".equals(args[j])) {
        j++;
        if(j == args.length) {
          System.err.println("-d option requires an argument");
          showHelp();
          System.exit(1);
        }
        baseDir = new File(args[j]);
      } else {
        long jobId = -1;
        try {
          jobId = Long.parseLong(args[j]);
        } catch(NumberFormatException e) {
          System.err.println("Job ID must be a valid number");
          System.exit(1);
        }
        job = mgr.getJob(jobId);
      }
    }

    if(job == null) {
      System.err.println("No such job");
      showHelp();
      System.exit(1);
    }

    List<Downloaded> downloads = doDownload(job.reports(), null, baseDir, jsonOutput, overwrite);
    if(jsonOutput) {
      mapper.writeValue(System.out, downloads);
    }
  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  download-all-reports <jobid> [-d directory] [-f]");
    System.err.println();
    System.err.println("Download all report and log files from a job into local files with");
    System.err.println("the corresponding names.  To download individual files use the");
    System.err.println("\"download\" command.");
    System.err.println();
    System.err.println(" <jobid> : the id of the job");
    System.err.println(" -d directory : directory into which the files should be downloaded.");
    System.err.println("                If omitted, default is the current directory.");
    System.err.println(" -f : force, i.e. if there is already a file in the output directory");
    System.err.println("      with the same name as a file to download, overwrite it.  If omitted");
    System.err.println("      the newly downloaded file will be renamed with a numeric suffix.");
  }


}
