/*
 * Copyright (c) 2022 The University of Sheffield
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

import org.apache.commons.io.FileUtils;
import uk.ac.gate.cloud.client.RestClientException;
import uk.ac.gate.cloud.common.Downloadable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract superclass for commands that involve downloading files via
 * API URLs that redirect to the real location in Amazon S3.
 */
public abstract class DownloadingCommand extends AbstractCommand {
  protected List<Downloaded> doDownload(List<Downloadable> entries, List<String> names, File baseDir, boolean jsonOutput, boolean overwrite) {
    int i = 0;
    List<Downloaded> downloaded = new ArrayList<>();
    for(Downloadable ent : entries) {
      String path = ent.url.getPath();
      String originalName = path.substring(path.lastIndexOf("/") + 1);
      String filename = names == null ? originalName : names.get(i);
      File f = new File(baseDir, filename);
      if(!overwrite && f.exists()) {
        int suffix = 1;
        String baseName = filename.endsWith(".gz") ? filename.substring(0, filename.length() - 3) : filename;
        int lastDot = baseName.lastIndexOf('.');
        if(lastDot >= 0) {
          baseName = baseName.substring(0, lastDot);
        } else {
          lastDot = baseName.length();
        }
        String extension = filename.substring(lastDot);
        while(f.exists()) {
          f = new File(baseDir, baseName + "(" + suffix + ")" + extension);
          suffix++;
        }
      }
      downloaded.add(new Downloaded(originalName, f.getPath()));
      if(!jsonOutput) {
        if(entries.size() > 1) {
          System.out.print("[" + (i + 1) + "/" + entries.size() + "] ");
        }
        System.out.println(originalName + " -> " + f.getPath());
      }
      try {
        FileUtils.copyURLToFile(ent.urlToDownload(), f);
      } catch(IOException e) {
        throw new RestClientException("Error downloading " + ent.url);
      }
      i++;
    }
    return downloaded;
  }

  /**
   * DTO class to represent a single downloaded file for use in the JSON output mode.
   */
  public static class Downloaded {
    public final String name;
    public final String destination;

    Downloaded(String name, String destination) {
      this.name = name;
      this.destination = destination;
    }
  }
}
