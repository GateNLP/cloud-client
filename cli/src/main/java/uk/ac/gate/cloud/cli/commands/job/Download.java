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
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;
import uk.ac.gate.cloud.client.RestClientException;
import uk.ac.gate.cloud.common.Downloadable;

public class Download extends AbstractCommand {

  public void run(RestClient client, String... args) throws Exception {
    if(args.length < 1) {
      System.err.println("Usage: download <URL>");
      System.exit(1);
    }
    Downloadable res = new Downloadable(new URL(args[0]));
    res.setClient(client);
    String path = res.url.getPath();
    String filename = path.substring(path.lastIndexOf("/") + 1);
    System.out.println(filename);
    File f = new File(filename);
    try {
      FileUtils.copyURLToFile(res.urlToDownload(), f);
    } catch(IOException e) {
      throw new RestClientException("Error downloading " + res.url);
    }
  }

}
