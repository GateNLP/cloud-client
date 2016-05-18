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
package uk.ac.gate.cloud.online;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import uk.ac.gate.cloud.client.StreamWritable;
import uk.ac.gate.cloud.common.ApiObject;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Class representing an endpoint for the online document processing
 * API.
 */
public class ApiEndpoint extends ApiObject {

  public String endpointUrl;

  /**
   * Pass a document to the endpoint and retrieve the annotated results.
   * 
   * @param content an input stream from which the document content can
   *          be read.
   * @param mimeType the MIME type identifying the document format, see
   *          the GATE documentation for a list of supported MIME types.
   * @param responseType requested response format
   * @param annotationSelectors annotation selector expressions
   *          specifying which annotation types to return.
   * @return an open <code>InputStream</code> containing the API
   *         response, this stream must be closed by the caller once it
   *         has been read.
   */
  public InputStream call(InputStream content, String mimeType,
          ResponseType responseType, String annotationSelectors) {
    return call(content, mimeType, responseType, annotationSelectors, true);
  }

  /**
   * Pass a document to the endpoint and retrieve the annotated results.
   * 
   * @param content an input stream from which the document content can
   *          be read..
   * @param mimeType the MIME type identifying the document format, see
   *          the GATE documentation for a list of supported MIME types.
   * @param responseType requested response format
   * @param annotationSelectors annotation selector expressions
   *          specifying which annotation types to return.
   * @param includeText should the response include the document text or
   *          just the annotations? Returning just annotations is only
   *          supported by the <code>GATE_XML</code> and
   *          <code>FINF</code> response types.
   * @param queryParameters additional query parameters to be appended
   *          to the endpoint URL, specified as an alternating sequence
   *          of keys and values. The keys and values will be
   *          URL-encoded by this method, they should not be encoded in
   *          advance by the caller.
   * @return an open <code>InputStream</code> containing the API
   *         response, this stream must be closed by the caller once it
   *         has been read.
   */
  public InputStream call(InputStream content, String mimeType,
          ResponseType responseType, String annotationSelectors,
          boolean includeText, String... queryParameters) {
    return doCall(content, mimeType, responseType, annotationSelectors,
            includeText, queryParameters);
  }

  /**
   * Pass a document to the endpoint and retrieve the annotated results.
   * 
   * @param content a {@link StreamWritable} that can write the document
   *          content to an output stream.
   * @param mimeType the MIME type identifying the document format, see
   *          the GATE documentation for a list of supported MIME types.
   * @param responseType requested response format
   * @param annotationSelectors annotation selector expressions
   *          specifying which annotation types to return.
   * @return an open <code>InputStream</code> containing the API
   *         response, this stream must be closed by the caller once it
   *         has been read.
   */
  public InputStream call(StreamWritable content, String mimeType,
          ResponseType responseType, String annotationSelectors) {
    return call(content, mimeType, responseType, annotationSelectors, true);
  }

  /**
   * Pass a document to the endpoint and retrieve the annotated results.
   * 
   * @param content a {@link StreamWritable} that can write the document
   *          content to an output stream.
   * @param mimeType the MIME type identifying the document format, see
   *          the GATE documentation for a list of supported MIME types.
   * @param responseType requested response format
   * @param annotationSelectors annotation selector expressions
   *          specifying which annotation types to return.
   * @param includeText should the response include the document text or
   *          just the annotations? Returning just annotations is only
   *          supported by the <code>GATE_XML</code> and
   *          <code>FINF</code> response types.
   * @param queryParameters additional query parameters to be appended
   *          to the endpoint URL, specified as an alternating sequence
   *          of keys and values. The keys and values will be
   *          URL-encoded by this method, they should not be encoded in
   *          advance by the caller.
   * @return an open <code>InputStream</code> containing the API
   *         response, this stream must be closed by the caller once it
   *         has been read.
   */
  public InputStream call(StreamWritable content, String mimeType,
          ResponseType responseType, String annotationSelectors,
          boolean includeText, String... queryParameters) {
    return doCall(content, mimeType, responseType, annotationSelectors,
            includeText, queryParameters);
  }

  private InputStream doCall(Object content, String mimeType,
          ResponseType responseType, String annotationSelectors,
          boolean includeText, String... queryParameters) {
    if(responseType == null) {
      responseType = ResponseType.JSON;
    }
    String[] extraHeaders = new String[6];
    int offs = 0;
    extraHeaders[offs++] = "Accept";
    extraHeaders[offs++] = responseType.getContentType(includeText);
    extraHeaders[offs++] = "Content-Type";
    extraHeaders[offs++] = mimeType;
    extraHeaders[offs++] = "Accept-Encoding";
    extraHeaders[offs++] = "gzip";
    String theUrl = endpointUrl;
    if(annotationSelectors != null || (queryParameters != null && queryParameters.length > 0)) {
      StringBuilder urlBuilder = new StringBuilder(theUrl);
      try {
        urlBuilder.append("?");
        boolean needsAnd = false;
        if(annotationSelectors != null) {
          urlBuilder.append("annotations=");
          urlBuilder.append(URLEncoder.encode(annotationSelectors, "UTF-8"));
          needsAnd = true;
        }
        if(queryParameters != null) {
          for(int i = 0; i < queryParameters.length; i+=2) {
            if(needsAnd) {
              urlBuilder.append("&");
            }
            urlBuilder.append(URLEncoder.encode(queryParameters[i], "UTF-8"));
            urlBuilder.append("=");
            urlBuilder.append(URLEncoder.encode(queryParameters[i+1], "UTF-8"));
            needsAnd = true;
          }
        }
      } catch(UnsupportedEncodingException e) {
        // can't happen
        throw new RuntimeException("JVM claims not to support UTF-8!", e);
      }
      theUrl = urlBuilder.toString();
    }
    return client.requestForStream(theUrl, "POST", content, 4096,
            extraHeaders);
  }
  
  public ServiceMetadata metadata() {
    return client.get(endpointUrl + "/metadata", new TypeReference<ServiceMetadata>() {
    });
  }
}
