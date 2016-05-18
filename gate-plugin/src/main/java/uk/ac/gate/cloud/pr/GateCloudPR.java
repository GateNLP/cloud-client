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
package uk.ac.gate.cloud.pr;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.TreeSet;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sun.xml.fastinfoset.stax.StAXDocumentParser;

import uk.ac.gate.cloud.client.RestClient;
import uk.ac.gate.cloud.client.RestClientException;
import uk.ac.gate.cloud.client.StreamWritable;
import uk.ac.gate.cloud.online.ApiEndpoint;
import uk.ac.gate.cloud.online.OnlineApiManager;
import uk.ac.gate.cloud.online.ResponseType;
import gate.FeatureMap;
import gate.Resource;
import gate.corpora.DocumentImpl;
import gate.corpora.DocumentStaxUtils;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ExecutionInterruptedException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.creole.metadata.Sharable;

/**
 * GATE PR to send a document to a GATE Cloud API endpoint and add the
 * resulting annotations to the document.
 */
@CreoleResource(name = "GATE Cloud API Client",
        comment = "Send documents to a GATE cloud pipeline and "
                + "add the resulting annotations to the document.")
public class GateCloudPR extends AbstractLanguageAnalyser {

  private static final long serialVersionUID = -6144867580409945714L;

  private URL endpointUrl;

  private String apiKey;

  private String apiPassword;

  private String annotationSelectors;

  private Integer minDelay = 501;

  private Boolean sendOnlyText = true;

  private FeatureMap annotationSetMapping;

  private ApiEndpoint endpoint;

  private long[] lastCallTime;

  public URL getEndpointUrl() {
    return endpointUrl;
  }

  @CreoleParameter(
          comment = "The API endpoint to call (copied from the pipeline's detail page)")
  public void setEndpointUrl(URL endpointUrl) {
    this.endpointUrl = endpointUrl;
  }

  public String getApiKey() {
    return apiKey;
  }

  @Optional
  @CreoleParameter(
          comment = "The API key identifier for authentication to the service. "
                  + "While this parameter is optional, operating without an "
                  + "API key will enforce much lower quotas than operating "
                  + "with one.")
  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getApiPassword() {
    return apiPassword;
  }

  @Optional
  @CreoleParameter(comment = "The corresponding password for the API key")
  public void setApiPassword(String apiPassword) {
    this.apiPassword = apiPassword;
  }

  public String getAnnotationSelectors() {
    return annotationSelectors;
  }

  @Optional
  @RunTime
  @CreoleParameter(
          comment = "Annotations that should be returned by the service")
  public void setAnnotationSelectors(String annotationSelectors) {
    this.annotationSelectors = annotationSelectors;
  }

  public Integer getMinDelay() {
    return minDelay;
  }

  @RunTime
  @CreoleParameter(
          comment = "Minimum delay in milliseconds to leave between calls to the service, "
                  + "to allow for rate limiting", defaultValue = "501")
  public void setMinDelay(Integer minDelay) {
    this.minDelay = minDelay;
  }

  public Boolean getSendOnlyText() {
    return sendOnlyText;
  }

  @RunTime
  @CreoleParameter(
          comment = "If true, send only the document text to the service.  If false, "
                  + "send the whole document including its features and any existing annotations.",
          defaultValue = "true")
  public void setSendOnlyText(Boolean sendOnlyText) {
    this.sendOnlyText = sendOnlyText;
  }

  public FeatureMap getAnnotationSetMapping() {
    return annotationSetMapping;
  }

  @Optional
  @RunTime
  @CreoleParameter(
          comment = "Mapping to rename annotation sets returned from the service. "
                  + "If unspecified, the annotation set names provided by the "
                  + "service will be used unchanged.")
  public void setAnnotationSetMapping(FeatureMap annotationSetMapping) {
    this.annotationSetMapping = annotationSetMapping;
  }

  /**
   * For internal use by the duplication mechanism only.
   */
  public ApiEndpoint getEndpoint() {
    return endpoint;
  }

  /**
   * For internal use by the duplication mechanism only.
   */
  @Sharable
  public void setEndpoint(ApiEndpoint endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * For internal use by the duplication mechanism only.
   */
  public long[] getLastCallTime() {
    return lastCallTime;
  }

  /**
   * For internal use by the duplication mechanism only.
   */
  @Sharable
  public void setLastCallTime(long[] lastCallTime) {
    this.lastCallTime = lastCallTime;
  }

  @Override
  public Resource init() throws ResourceInstantiationException {
    if(endpointUrl == null) {
      throw new ResourceInstantiationException("No endpoint URL specified");
    }
    if(endpoint == null) {
      RestClient client = new RestClient(apiKey, apiPassword);
      OnlineApiManager mgr = new OnlineApiManager(client);
      endpoint = mgr.getEndpoint(endpointUrl.toString());
      lastCallTime = new long[] {0L};
    }
    return this;
  }

  @Override
  public void reInit() throws ResourceInstantiationException {
    endpoint = null;
    lastCallTime = null;
    init();
  }

  public void execute() throws ExecutionException {
    if(isInterrupted()) {
      throw new ExecutionInterruptedException();
    }
    interrupted = false;

    String mimeType;
    StreamWritable content;
    if(sendOnlyText) {
      mimeType = "text/plain; charset=UTF-8";
      content = new TextWritable(document.getContent().toString());
    } else {
      mimeType = "application/fastinfoset";
      content = new FINFWritable(document);
    }

    InputStream response;
    String[] extraParameters = null;

    if(document instanceof DocumentImpl) {
      // ensure we don't get a clashing annotation ID back from the
      // service
      extraParameters =
              new String[] {
                  "nextAnnotationId",
                  String.valueOf(((DocumentImpl)document)
                          .peakAtNextAnnotationId())};
    }

    try {
      synchronized(endpoint) {
        // ensure we wait at least minDelay ms since the last call
        // before making the next one
        long timeSinceLastCall = System.currentTimeMillis() - lastCallTime[0];
        long requiredDelay = minDelay - timeSinceLastCall;
        if(requiredDelay > 0) {
          try {
            Thread.sleep(requiredDelay);
          } catch(InterruptedException e) {
            throw new ExecutionInterruptedException();
          }
        }
        response =
                endpoint.call(content, mimeType, ResponseType.FINF,
                        annotationSelectors, false, extraParameters);
        lastCallTime[0] = System.currentTimeMillis();
      }
    } catch(RestClientException rce) {
      throw new ExecutionException("Error calling GATE Cloud service", rce);
    }

    // parse the response
    XMLStreamReader xsr = new StAXDocumentParser(response);
    try {
      try {
        xsr.nextTag(); // should be GateDocument
        while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
          DocumentStaxUtils.readAnnotationSet(xsr, document
                  .getAnnotations(mappedASName(xsr.getAttributeValue(null,
                          "Name"))), null, new TreeSet<Integer>(), null);
        }
      } finally {
        xsr.close();
        response.close();
      }
    } catch(XMLStreamException e) {
      throw new ExecutionException("Error parsing result from service", e);
    } catch(IOException e) {
      throw new ExecutionException("Error parsing result from service", e);
    }
  }

  protected String mappedASName(String name) {
    if(annotationSetMapping != null && annotationSetMapping.containsKey(name)) {
      return (String)annotationSetMapping.get(name);
    } else {
      return name;
    }
  }
}
