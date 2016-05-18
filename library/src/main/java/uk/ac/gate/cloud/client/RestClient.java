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
package uk.ac.gate.cloud.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Client responsible for communication with the GATE Cloud API. Handles
 * authentication, and serialization and deserialization of JSON request
 * and response bodies.
 * 
 * @author Ian Roberts
 * 
 */
public class RestClient {

  private final ObjectMapper MAPPER = new ObjectMapper().disable(
          DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .setInjectableValues(
                  new InjectableValues.Std().addValue(RestClient.class, this));

  /**
   * The standard base URI for the GATE Cloud API.
   */
  public static final URL DEFAULT_BASE_URL;
  static {
    try {
      DEFAULT_BASE_URL = new URL("https://cloud.gate.ac.uk/api/");
    } catch(MalformedURLException e) {
      // can't happen
      throw new ExceptionInInitializerError(e);
    }
  }

  /**
   * The HTTP basic authentication header that will be appended to all
   * requests.
   */
  private String authorizationHeader;

  /**
   * The base URL that will be used to resolve any relative request
   * URIs.
   */
  private URL baseUrl;

  /**
   * Allow access to the response headers from the most recent call.
   */
  private ThreadLocal<Map<String, List<String>>> lastResponseHeaders =
          new ThreadLocal<>();

  /**
   * Create a client that uses the {@link #DEFAULT_BASE_URL default base
   * URL}.
   * 
   * @param apiKeyId API key identifier for authentication. If null, no
   *          authentication will be used, which means the client can
   *          only be used to call public pipelines via the online API.
   * @param apiPassword API key password
   */
  public RestClient(String apiKeyId, String apiPassword) {
    this(DEFAULT_BASE_URL, apiKeyId, apiPassword);
  }

  /**
   * Create a client using a specified base URL (for advanced use only -
   * the default URL will work for all normal cases).
   * 
   * @param url API base URL
   * @param apiKeyId API key identifier for authentication
   * @param apiPassword API key password
   */
  public RestClient(URL url, String apiKeyId, String apiPassword) {
    baseUrl = url;
    if(apiKeyId != null) {
      try {
        // HTTP header is "Basic base64(username:password)"
        authorizationHeader =
                "Basic "
                        + DatatypeConverter
                                .printBase64Binary((apiKeyId + ":" + apiPassword)
                                        .getBytes("UTF-8"));
      } catch(UnsupportedEncodingException e) {
        // should never happen
        throw new RuntimeException("JVM claims not to support UTF-8 encoding...",
                e);
      }
    }
  }

  public URL getBaseUrl() {
    return baseUrl;
  }

  /**
   * Make an API request and parse the JSON response into a new object.
   * 
   * @param target the URL to request (relative URLs will resolve
   *          against the {@link #getBaseUrl() base URL}).
   * @param method the request method (GET, POST, DELETE, etc.)
   * @param responseType the Java type corresponding to a successful
   *          response message for this URL
   * @param requestBody the value to send as the request body. If
   *          <code>null</code>, no request body is sent. If an
   *          <code>InputStream</code> or a <code>StreamWriteable</code>
   *          then its content will be sent as-is and an appropriate
   *          <code>Content-Type</code> should be given in the
   *          <code>extraHeaders</code> (note that the input stream will
   *          <em>not</em> be closed by this method, that is the
   *          responsibility of the caller). Otherwise the provided
   *          object will be serialized to JSON and sent with the
   *          default <code>application/json</code> MIME type.
   * @param extraHeaders any additional HTTP headers, specified as an
   *          alternating sequence of header names and values
   * @return for a successful response, the deserialized response body,
   *         or <code>null</code> for a 201 response
   * @throws RestClientException if an exception occurs during
   *           processing, or the server returns a 4xx or 5xx error
   *           response (in which case the response JSON message will be
   *           available as a {@link JsonNode} in the exception).
   */
  public <T> T request(String target, String method,
          TypeReference<T> responseType, Object requestBody,
          String... extraHeaders) throws RestClientException {
    try {
      HttpURLConnection connection =
              sendRequest(target, method, requestBody, -1, extraHeaders);
      return readResponseOrError(connection, responseType);
    } catch(IOException e) {
      throw new RestClientException(e);
    }
  }

  /**
   * Make an API request and return the raw data from the response as an
   * InputStream.
   * 
   * @param target the URL to request (relative URLs will resolve
   *          against the {@link #getBaseUrl() base URL}).
   * @param method the request method (GET, POST, DELETE, etc.)
   * @param requestBody the value to send as the request body. If
   *          <code>null</code>, no request body is sent. If an
   *          <code>InputStream</code> or a <code>StreamWriteable</code>
   *          then its content will be sent as-is and an appropriate
   *          <code>Content-Type</code> should be given in the
   *          <code>extraHeaders</code> (note that the input stream will
   *          <em>not</em> be closed by this method, that is the
   *          responsibility of the caller). Otherwise the provided
   *          object will be serialized to JSON and sent with the
   *          default <code>application/json</code> MIME type.
   * @param gzipThreshold size threshold above which the request body
   *          should be GZIP compressed. If negative, the request will
   *          never be compressed.
   * @param extraHeaders any additional HTTP headers, specified as an
   *          alternating sequence of header names and values
   * @return for a successful response, the response stream, or
   *         <code>null</code> for a 201 response
   * @throws RestClientException if an exception occurs during
   *           processing, or the server returns a 4xx or 5xx error
   *           response (in which case the response JSON message will be
   *           available as a {@link JsonNode} in the exception).
   */
  public InputStream requestForStream(String target, String method,
          Object requestBody, int gzipThreshold, String... extraHeaders)
          throws RestClientException {
    try {
      HttpURLConnection connection =
              sendRequest(target, method, requestBody, gzipThreshold,
                      extraHeaders);
      int responseCode = connection.getResponseCode();
      if(responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
        // successful response with no content
        return null;
      } else if(responseCode >= 400) {
        readError(connection);
        return null; // not reachable, readError always throws exception
      } else if(responseCode >= 300) {
        // redirect - all redirects we care about from the GATE Cloud
        // APIs are 303. We have to follow them manually to make
        // authentication work properly.
        String location = connection.getHeaderField("Location");
        // consume body
        InputStream stream = connection.getInputStream();
        IOUtils.copy(stream, new NullOutputStream());
        IOUtils.closeQuietly(stream);
        // follow the redirect
        return requestForStream(location, method, requestBody, gzipThreshold,
                extraHeaders);
      } else {
        storeHeaders(connection);
        if("gzip".equals(connection.getHeaderField("Content-Encoding"))) {
          return new GZIPInputStream(connection.getInputStream());
        } else {
          return connection.getInputStream();
        }
      }
    } catch(IOException e) {
      throw new RestClientException(e);
    }

  }

  /**
   * Make an API request and parse the JSON response, using the response
   * to update the state of an existing object.
   * 
   * @param target the URL to request (relative URLs will resolve
   *          against the {@link #getBaseUrl() base URL}).
   * @param method the request method (GET, POST, DELETE, etc.)
   * @param responseObject the Java object to update from a successful
   *          response message for this URL
   * @param requestBody the value to send as the request body. If
   *          <code>null</code>, no request body is sent. If an
   *          <code>InputStream</code> or a <code>StreamWriteable</code>
   *          then its content will be sent as-is and an appropriate
   *          <code>Content-Type</code> should be given in the
   *          <code>extraHeaders</code> (note that the input stream will
   *          <em>not</em> be closed by this method, that is the
   *          responsibility of the caller). Otherwise the provided
   *          object will be serialized to JSON and sent with the
   *          default <code>application/json</code> MIME type.
   * @param extraHeaders any additional HTTP headers, specified as an
   *          alternating sequence of header names and values
   * @throws RestClientException if an exception occurs during
   *           processing, or the server returns a 4xx or 5xx error
   *           response (in which case the response JSON message will be
   *           available as a {@link JsonNode} in the exception).
   */
  public void requestForUpdate(String target, String method,
          Object responseObject, Object requestBody, String... extraHeaders)
          throws RestClientException {
    try {
      HttpURLConnection connection =
              sendRequest(target, method, requestBody, -1, extraHeaders);
      readResponseOrErrorForUpdate(connection, responseObject);
    } catch(IOException e) {
      throw new RestClientException(e);
    }
  }

  /**
   * Handles the sending side of an HTTP request, returning a connection
   * from which the response (or error) can be read.
   */
  private HttpURLConnection sendRequest(String target, String method,
          Object requestBody, int gzipThreshold, String... extraHeaders)
          throws IOException {
    URL requestUrl = new URL(baseUrl, target);
    HttpURLConnection connection =
            (HttpURLConnection)requestUrl.openConnection();
    connection.setRequestMethod(method);
    connection.setInstanceFollowRedirects(false);
    if(authorizationHeader != null) {
      connection.setRequestProperty("Authorization", authorizationHeader);
    }
    boolean sentAccept = false;
    boolean sentContentType = false;
    if(extraHeaders != null) {
      for(int i = 0; i < extraHeaders.length; i++) {
        if("Accept".equalsIgnoreCase(extraHeaders[i])) {
          sentAccept = true;
        }
        if("Content-Type".equalsIgnoreCase(extraHeaders[i])) {
          sentContentType = true;
        }
        connection.setRequestProperty(extraHeaders[i], extraHeaders[++i]);
      }
    }
    if(!sentAccept) {
      connection.setRequestProperty("Accept", "application/json");
    }
    if(requestBody != null) {
      connection.setDoOutput(true);
      if(!sentContentType) {
        connection.setRequestProperty("Content-Type", "application/json");
      }
      OutputStream out;
      if(gzipThreshold >= 0) {
        out = new GZIPThresholdOutputStream(connection, gzipThreshold);
      } else {
        out = connection.getOutputStream();
      }
      try {
        if(requestBody instanceof InputStream) {
          IOUtils.copy((InputStream)requestBody, out);
        } else if(requestBody instanceof StreamWritable) {
          ((StreamWritable)requestBody).writeTo(out);
        } else {
          MAPPER.writeValue(out, requestBody);
        }
      } finally {
        out.close();
      }
    }
    return connection;
  }

  /**
   * Read a response or error message from the given connection,
   * handling any 303 redirect responses.
   */
  private <T> T readResponseOrError(HttpURLConnection connection,
          TypeReference<T> responseType) throws RestClientException {
    return readResponseOrError(connection, responseType, true);
  }

  /**
   * Read a response or error message from the given connection,
   * handling any 303 redirect responses if <code>followRedirects</code>
   * is true.
   */
  private <T> T readResponseOrError(HttpURLConnection connection,
          TypeReference<T> responseType, boolean followRedirects)
          throws RestClientException {
    InputStream stream = null;
    try {
      int responseCode = connection.getResponseCode();
      if(responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
        // successful response with no content
        storeHeaders(connection);
        return null;
      }
      String encoding = connection.getContentEncoding();
      if("gzip".equalsIgnoreCase(encoding)) {
        stream = new GZIPInputStream(connection.getInputStream());
      } else {
        stream = connection.getInputStream();
      }

      if(responseCode < 300 || responseCode >= 400 || !followRedirects) {
        try {
          return MAPPER.readValue(stream, responseType);
        } finally {
          storeHeaders(connection);
          stream.close();
        }
      } else {
        // redirect - all redirects we care about from the GATE Cloud
        // APIs are 303. We have to follow them manually to make
        // authentication work properly.
        String location = connection.getHeaderField("Location");
        // consume body
        IOUtils.copy(stream, new NullOutputStream());
        IOUtils.closeQuietly(stream);
        // follow the redirect
        return get(location, responseType);
      }
    } catch(Exception e) {
      readError(connection);
      return null; // unreachable, as readError always throws exception
    }
  }

  /**
   * Read a response or error message from the given connection, and
   * update the state of the given object.
   */
  private void readResponseOrErrorForUpdate(HttpURLConnection connection,
          Object responseObject) throws RestClientException {
    InputStream stream = null;
    try {
      if(connection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
        // successful response with no content
        return;
      }
      stream = connection.getInputStream();
      try {
        MAPPER.readerForUpdating(responseObject).readValue(stream);
      } finally {
        stream.close();
      }
    } catch(Exception e) {
      readError(connection);
    }
  }

  /**
   * Read an error response from the given connection and throw a
   * suitable {@link RestClientException}. This method always throws an
   * exception, it will never return normally.
   */
  private void readError(HttpURLConnection connection)
          throws RestClientException {
    InputStream stream;
    try {
      String encoding = connection.getContentEncoding();
      if("gzip".equalsIgnoreCase(encoding)) {
        stream = new GZIPInputStream(connection.getInputStream());
      } else {
        stream = connection.getInputStream();
      }

      InputStreamReader reader = new InputStreamReader(stream, "UTF-8");

      try {
        JsonNode errorNode = null;
        errorNode = MAPPER.readTree(stream);

        throw new RestClientException("Server returned response code "
                + connection.getResponseCode(), errorNode,
                connection.getResponseCode(), connection.getHeaderFields());

      } finally {
        reader.close();
      }
    } catch(RestClientException e2) {
      throw e2;
    } catch(Exception e2) {
      throw new RestClientException("Error communicating with server", e2);
    }
  }

  /**
   * Perform an HTTP GET request, parsing the JSON response to create a
   * new object.
   * 
   * @param target the URL to request (relative URLs will resolve
   *          against the {@link #getBaseUrl() base URL}).
   * @param responseType the Java type corresponding to a successful
   *          response message for this URL
   * @return for a successful response, the deserialized response body,
   *         or <code>null</code> for a 201 response
   * @throws RestClientException if an exception occurs during
   *           processing, or the server returns a 4xx or 5xx error
   *           response (in which case the response JSON message will be
   *           available as a {@link JsonNode} in the exception).
   */
  public <T> T get(String target, TypeReference<T> responseType)
          throws RestClientException {
    return request(target, "GET", responseType, null);
  }

  /**
   * Perform an HTTP GET request, parsing the JSON response to update
   * the state of an existing object.
   * 
   * @param target the URL to request (relative URLs will resolve
   *          against the {@link #getBaseUrl() base URL}).
   * @param responseObject the Java object to update from a successful
   *          response message for this URL
   * @throws RestClientException if an exception occurs during
   *           processing, or the server returns a 4xx or 5xx error
   *           response (in which case the response JSON message will be
   *           available as a {@link JsonNode} in the exception).
   */
  public void getForUpdate(String target, Object responseObject)
          throws RestClientException {
    requestForUpdate(target, "GET", responseObject, null);
  }

  /**
   * Perform an HTTP POST request, parsing the JSON response to create a
   * new object.
   * 
   * @param target the URL to request (relative URLs will resolve
   *          against the {@link #getBaseUrl() base URL}).
   * @param responseType the Java type corresponding to a successful
   *          response message for this URL
   * @param requestBody the value to send as the request body. If
   *          <code>null</code>, no request body is sent. If an
   *          <code>InputStream</code> or a <code>StreamWriteable</code>
   *          then its content will be sent as-is and an appropriate
   *          <code>Content-Type</code> should be given in the
   *          <code>extraHeaders</code> (note that the input stream will
   *          <em>not</em> be closed by this method, that is the
   *          responsibility of the caller). Otherwise the provided
   *          object will be serialized to JSON and sent with the
   *          default <code>application/json</code> MIME type.
   * @return for a successful response, the deserialized response body,
   *         or <code>null</code> for a 201 response
   * @throws RestClientException if an exception occurs during
   *           processing, or the server returns a 4xx or 5xx error
   *           response (in which case the response JSON message will be
   *           available as a {@link JsonNode} in the exception).
   */
  public <T> T post(String target, TypeReference<T> responseType,
          Object requestBody) throws RestClientException {
    return request(target, "POST", responseType, requestBody);
  }

  /**
   * Perform an HTTP POST request, parsing the JSON response to update
   * the state of an existing object.
   * 
   * @param target the URL to request (relative URLs will resolve
   *          against the {@link #getBaseUrl() base URL}).
   * @param responseObject the Java object to update from a successful
   *          response message for this URL
   * @param requestBody the value to send as the request body. If
   *          <code>null</code>, no request body is sent. If an
   *          <code>InputStream</code> or a <code>StreamWriteable</code>
   *          then its content will be sent as-is and an appropriate
   *          <code>Content-Type</code> should be given in the
   *          <code>extraHeaders</code> (note that the input stream will
   *          <em>not</em> be closed by this method, that is the
   *          responsibility of the caller). Otherwise the provided
   *          object will be serialized to JSON and sent with the
   *          default <code>application/json</code> MIME type.
   * @throws RestClientException if an exception occurs during
   *           processing, or the server returns a 4xx or 5xx error
   *           response (in which case the response JSON message will be
   *           available as a {@link JsonNode} in the exception).
   */
  public void postForUpdate(String target, Object responseObject,
          Object requestBody) throws RestClientException {
    requestForUpdate(target, "POST", responseObject, requestBody);
  }

  /**
   * Perform an HTTP DELETE request for the given resource.
   * 
   * @param target the URL to request (relative URLs will resolve
   *          against the {@link #getBaseUrl() base URL}).
   * @throws RestClientException if an exception occurs during
   *           processing, or the server returns a 4xx or 5xx error
   *           response (in which case the response JSON message will be
   *           available as a {@link JsonNode} in the exception).
   */
  public void delete(String target) throws RestClientException {
    request(target, "DELETE", new TypeReference<JsonNode>() {
    }, null);
  }

  /**
   * Perform an HTTP GET request on a URL whose response is expected to
   * be a 3xx redirection, and return the target redirection URL.
   * 
   * @param source the URL to request (relative URLs will resolve
   *          against the {@link #getBaseUrl() base URL}).
   * @return the URL returned by the "Location" header of the
   *         redirection response.
   * @throws RestClientException if an exception occurs during
   *           processing, or the server returns a 4xx or 5xx error
   *           response (in which case the response JSON message will be
   *           available as a {@link JsonNode} in the exception), or if
   *           the response was not a 3xx redirection.
   */
  public URL getRedirect(URL source) throws RestClientException {
    try {
      HttpURLConnection connection = (HttpURLConnection)source.openConnection();
      connection.setRequestMethod("GET");
      if(authorizationHeader != null) {
        connection.setRequestProperty("Authorization", authorizationHeader);
      }
      connection.setRequestProperty("Accept", "application/json");
      connection.setInstanceFollowRedirects(false);
      int responseCode = connection.getResponseCode();
      // make sure we read any response content
      readResponseOrError(connection, new TypeReference<JsonNode>() {
      }, false);
      if(responseCode >= 300 && responseCode < 400) {
        // it was a redirect
        String redirectUrl = connection.getHeaderField("Location");
        return new URL(redirectUrl);
      } else {
        throw new RestClientException("Expected redirect but got "
                + responseCode);
      }
    } catch(IOException e) {
      throw new RestClientException(e);
    }
  }

  private void storeHeaders(HttpURLConnection connection) {
    lastResponseHeaders.set(connection.getHeaderFields());
  }

  /**
   * Returns the response headers that were sent with the most recent
   * successful HTTP response received by this client in this thread.
   * 
   * @return the headers, as a map where the keys are HTTP header names
   *         and the values are the list (usually singleton) of values
   *         for that header.
   */
  public Map<String, List<String>> getLastHeaders() {
    return lastResponseHeaders.get();
  }
}
