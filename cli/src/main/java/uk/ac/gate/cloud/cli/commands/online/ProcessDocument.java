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
package uk.ac.gate.cloud.cli.commands.online;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;
import uk.ac.gate.cloud.online.ApiEndpoint;
import uk.ac.gate.cloud.online.OnlineApiManager;
import uk.ac.gate.cloud.online.ResponseType;

public class ProcessDocument extends AbstractCommand {

  @Override
  public void run(RestClient client, String... args) throws Exception {
    if(args.length < 1) {
      showHelp();
      System.exit(1);
    }
    OnlineApiManager mgr = new OnlineApiManager(client);
    ApiEndpoint endpoint = mgr.getEndpoint(args[0]);
    
    InputStream content = System.in;
    String mimeType = "text/plain";
    ResponseType responseType = ResponseType.JSON;
    String annotationSelectors = null;
    boolean includeText = true;
    Path outputFile = null;
    boolean showQuotaInfo = false;
    
    int i;
    for(i = 1; i+1 < args.length && args[i].startsWith("-"); i++) {
      if("-in".equals(args[i])) {
        content = Files.newInputStream(Paths.get(args[++i]));
      } else if("-out".equals(args[i])) {
        outputFile = Paths.get(args[++i]);
      } else if("-contentType".equals(args[i])) {
        mimeType = args[++i];
      } else if("-responseType".equals(args[i])) {
        try {
          responseType = ResponseType.valueOf(args[++i]);
        } catch(IllegalArgumentException e) {
          System.err.println("Unrecognised response type.");
          showHelp();
          System.exit(1);
        }
      } else if("-includeText".equals(args[i])) {
        includeText = !"no".equals(args[++i]);
      } else if("-annotationSelectors".equals(args[i])) {
        annotationSelectors = args[++i];
      } else if("-showQuota".equals(args[i])) {
        showQuotaInfo = true;
      } else {
        showHelp();
        System.exit(1);
      }
    }
    
    try(InputStream result = endpoint.call(content, mimeType, responseType, annotationSelectors, includeText)) {
      try(OutputStream out = (outputFile == null ? System.out : Files.newOutputStream(outputFile))) {
        IOUtils.copy(result, out);
      }
    }
    
    if(showQuotaInfo) {
      Map<String, List<String>> headers = client.getLastHeaders();
      System.err.println("Quota information");
      System.err.println();
      List<String> reqCost = headers.get("X-GATE-Request-Cost");
      if(reqCost != null && reqCost.size() > 0) {
        System.err.println("                Cost of this request: " + reqCost.get(0));
      }
      List<String> remainingQuota = headers.get("X-GATE-Remaining-Quota");
      if(remainingQuota != null && remainingQuota.size() > 0) {
        System.err.println("           Remaining quota for today: " + remainingQuota.get(0));
      }
      List<String> quotaReset = headers.get("X-GATE-Quota-Reset");
      if(quotaReset != null && quotaReset.size() > 0) {
        System.err.println("                 Quota will reset at: " + quotaReset.get(0));
      }
      List<String> rateLimit = headers.get("X-GATE-Rate-Limit-Calls");
      if(rateLimit != null && rateLimit.size() > 0) {
        System.err.println("Remaining calls in rate limit window: " + rateLimit.get(0));
      }
      List<String> limitReset = headers.get("X-GATE-Rate-Limit-Reset");
      if(limitReset != null && limitReset.size() > 0) {
        System.err.println("           Rate limit window ends at: " + limitReset.get(0));
      }
    }
  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage: process-document <endpoint> [options]");
    System.err.println();
    System.err.println("Available options:");
    System.err.println("  -in <file> : file containing the text to process. If omitted the");
    System.err.println("              data is read from standard input.");
    System.err.println("  -out <file> : file to which output should be written. If omitted");
    System.err.println("              the result will be sent to standard output.");
    System.err.println("  -contentType : MIME type identifying the format of the document.");
    System.err.println("              If omitted, \"text/plain\" is assumed.");
    System.err.println("  -responseType : desired response format, available choices are:");
    System.err.println("              JSON - GATE's JSON format, based on the format used");
    System.err.println("                     by Twitter to represent hashtags, etc.");
    System.err.println("              GATE_XML - GATE standoff XML format.");
    System.err.println("              FINF - FastInfoset binary serialization of GATE XML.");
    System.err.println("              If omitted, JSON is assumed.");
    System.err.println("  -includeText <yes|no> - the GATE_XML and FINF response formats");
    System.err.println("              offer the option to return just the annotations without");
    System.err.println("              the document text.  Note that the annotation offsets");
    System.err.println("              are only likely to be meaningful if the original document");
    System.err.println("              was plain text or GATE XML. If omitted, text is included.");
    System.err.println("  -annotationSelectors : Comma-separated annotation selector");
    System.err.println("              expressions to select which annotations to include.");
    System.err.println("              Each expression takes the form setName:type, where");
    System.err.println("              an empty set name means the default set and an empty");
    System.err.println("              type means all annotations from that set (so \":\" on its");
    System.err.println("              own means all annotation from the default set).");
    System.err.println("              If omitted the pipeline's default selectors will be used.");
    System.err.println("  -showQuota: write quota information to standard error.");
  }
}
