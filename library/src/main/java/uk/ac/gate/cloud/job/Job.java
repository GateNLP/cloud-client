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
package uk.ac.gate.cloud.job;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import uk.ac.gate.cloud.common.Downloadable;
import uk.ac.gate.cloud.common.Prices;
import uk.ac.gate.cloud.data.DataBundle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Full details of an annotation job, and methods to configure and
 * control the job. Note that while many of the fields on this object
 * are public, direct modifications to the field values will not be
 * reflected in the server-side state - use the manipulation methods to
 * modify the job on the server.
 * 
 * @author Ian Roberts
 */
public class Job extends JobSummary {

  /**
   * The job's numeric identifier.
   */
  public long id;

  /**
   * The name of the job.
   */
  public String name;

  /**
   * Internal unique identifier for the job, used for example in
   * temporary file names.
   */
  public String uuid;

  /**
   * The prices that will be charged when running this job.
   */
  public Prices price;

  /**
   * Date the job was created, in the form of {@link Date#toString()}.
   */
  public String dateCreated;

  /**
   * Date the job completed execution, in the form of
   * {@link Date#toString()}, or <code>null</code> if the job has not
   * yet been run.
   */
  public String dateCompleted;

  /**
   * Date when the results of the completed job will expire and be
   * deleted from temporary storage. If your job has not been configured
   * to store its results directly in your own S3 bucket then you must
   * download them before this time or they will be lost. This field
   * will be <code>null</code> if the job has not yet been run.
   */
  public String resultsAvailableUntil;

  /**
   * Processing time consumed so far by this job, measured in
   * milliseconds.
   */
  public long timeUsed;

  /**
   * Processing time that has been charged so far for this job. Measured
   * in milliseconds but the value will always be a multiple of one hour
   * (3600000 ms) and may be more than {@link #timeUsed} as charges are
   * rounded up to whole hours.
   */
  public long timeCharged;

  /**
   * Number of bytes of data processed so far by this job.
   */
  public long bytesUsed;

  /**
   * Number of bytes of data processing that has so far been charged to
   * your account for this job.
   */
  public long bytesCharged;

  /**
   * Number between 0 and 1 giving the proportion of the job's tasks
   * that have so far been completed, or less than 0 if the number of
   * sub-tasks has not yet been determined.
   */
  public double progress;

  /**
   * URL of the data bundle containing the results of the most recent
   * run of this job. May be null if the job has not completed, or if
   * its result bundle has already been deleted.
   */
  public String resultBundle;

  /**
   * Change the name of this job.
   * 
   * @param newName the new name
   */
  public void rename(String newName) {
    ObjectNode request = JsonNodeFactory.instance.objectNode();
    request.put("name", newName);
    client.postForUpdate(url, this, request);
  }

  /**
   * Configure this job to send its output files directly to an S3
   * bucket that is not owned by GATE Cloud rather than creating
   * a new data bundle.
   * 
   * @param location a "URL" of the form
   *          <code>s3://bucketname/keyprefix/</code> denoting the
   *          target bucket and prefix to prepend to key values when
   *          storing objects in the bucket. If the given location does
   *          not end with a slash, one will be added.
   * @param accessKeyId an AWS access key ID (typically a limited IAM
   *          user) with permission to put objects in the specified
   *          bucket with keys starting with the specified prefix
   * @param secretKey the corresponding AWS secret key.
   */
  public void outputToS3(String location, String accessKeyId, String secretKey) {
    if(!location.endsWith("/")) {
      location += "/";
    }
    ObjectNode request = JsonNodeFactory.instance.objectNode();
    request.put("s3Location", location);
    request.put("accessKey", accessKeyId);
    request.put("secretKey", secretKey);
    client.post(url + "/outputDirectory", new TypeReference<JsonNode>() {
    }, request);
  }

  /**
   * Configure this job to use the default behaviour of saving its
   * output in GATE Cloud data bundle. This call can be used to
   * countermand an earlier {@link #outputToS3}.
   */
  public void outputToDefault() {
    ObjectNode request = JsonNodeFactory.instance.objectNode();
    request.put("defaultLocation", true);
    client.post(url + "/outputDirectory", new TypeReference<JsonNode>() {
    }, request);
  }

  /**
   * List all the input specifications configured for this job.
   * 
   * @return one {@link InputSummary} object per input specification
   */
  public List<InputSummary> listInputs() {
    return client.get(url + "/input", new TypeReference<List<InputSummary>>() {
    });
  }

  /**
   * Add an input specification to this job, taking input from a data
   * bundle. All configuration parameters for the input specification
   * are taken from the bundle.
   * 
   * @param bundleId the ID of the input bundle.
   * @return details of the newly-created input specification.
   */
  public InputDetails addBundleInput(long bundleId) {
    ObjectNode request = JsonNodeFactory.instance.objectNode();
    request.put("sourceBundle", bundleId);
    // create the input
    return client.post(url + "/input", new TypeReference<InputDetails>() {
    }, request);

  }

  /**
   * List all the output specifications for this job.
   * 
   * @return one {@link Output} object per output specification
   */
  public List<Output> listOutputs() {
    return client.get(url + "/output", new TypeReference<List<Output>>() {
    });
  }

  /**
   * Add an output specification for this job that pushes results into a
   * M&iacute;mir index.
   * 
   * @param indexUrl URL of the target index
   * @param username username used to authenticate to the index. If
   *          <code>null</code> authentication is not used.
   * @param password the corresponding password (should be
   *          <code>null</code> if and only if username is
   *          <code>null</code>)
   * @return details of the newly-created output specification.
   */
  public Output addMimirOutput(String indexUrl, String username, String password) {
    ObjectNode request = JsonNodeFactory.instance.objectNode();
    request.put("type", OutputType.MIMIR.name());
    request.put("indexUrl", indexUrl);
    if(username != null) {
      request.put("username", username);
      request.put("password", password);
    }
    return client.post(url + "/output", new TypeReference<Output>() {
    }, request);
  }

  /**
   * Add an output specification for this job that saves results in
   * files (which will be packaged up and delivered as ZIP archives) in
   * one of a number of formats.
   * 
   * @param type output type
   * @param fileExtension the extension to append to the generated file
   *          names (e.g. ".GATE.xml"). This should be different for
   *          each output specification.
   * @param annotationSelectors comma-separated list of
   *          {@link Output#annotationSelectors annotation selector
   *          expressions}.
   * @return details of the newly-created output specification.
   */
  public Output addFileOutput(OutputType type, String fileExtension,
          String annotationSelectors) {
    ObjectNode request = JsonNodeFactory.instance.objectNode();
    request.put("type", type.name());
    request.put("fileExtension", fileExtension);
    if(annotationSelectors != null) {
      request.put("annotationSelectors", annotationSelectors);
    }
    return client.post(url + "/output", new TypeReference<Output>() {
    }, request);
  }

  /**
   * Add an output specification for this job that saves results as
   * JSON, in gzip-compressed bundles concatenated together (with one
   * JSON object per document).
   * 
   * @param annotationSelectors comma-separated list of
   *          {@link Output#annotationSelectors annotation selector
   *          expressions}.
   * @return details of the newly-created output specification.
   */
  public Output addJSONOutput(String annotationSelectors) {
    return addFileOutput(OutputType.JSON, null, annotationSelectors);
  }

  /**
   * Common implementation for job control actions.
   */
  protected void control(String action) {
    ObjectNode request = JsonNodeFactory.instance.objectNode();
    request.put("action", action);
    client.post(url + "/control", new TypeReference<JsonNode>() {
    }, request);
    refresh();
  }

  /**
   * Start the execution of this job. The job must be in the
   * {@link JobState#READY READY} state or this operation will fail.
   */
  public void start() {
    control("start");
  }

  /**
   * Stop the execution of this job. The job must be in the
   * {@link JobState#ACTIVE ACTIVE} state or this operation will fail.
   */
  public void stop() {
    control("stop");
  }

  /**
   * Resume execution of this job after it was suspended due to lack of
   * funds. The job must be in the {@link JobState#SUSPENDED SUSPENDED}
   * state or this operation will fail.
   */
  public void resume() {
    control("resume");
  }

  /**
   * Reset this job so it can be re-run. The job must be in the
   * {@link JobState#COMPLETED COMPLETED} state or this operation will
   * fail.
   */
  public void reset() {
    control("reset");
  }

  /**
   * Fetch execution log messages for this job. The <code>from</code>
   * and <code>to</code> parameters allow you to restrict to log entries
   * within a specific window of time - either or both of these
   * parameters may be <code>null</code>.
   * 
   * @param from earliest time stamp for log events to retrieve.
   * @param to latest time stamp for log events to retrieve.
   * @return log messages within the specified range
   */
  public List<LogMessage> executionLog(Calendar from, Calendar to) {
    StringBuilder urlBuilder = new StringBuilder(url);
    urlBuilder.append("/log");
    try {
      if(from != null) {
        urlBuilder.append("?from=");
        urlBuilder.append(URLEncoder.encode(
                DatatypeConverter.printDateTime(from), "UTF-8"));
      }
      if(to != null) {
        if(from == null) {
          urlBuilder.append("?to=");
        } else {
          urlBuilder.append("&to=");
        }
        urlBuilder.append(URLEncoder.encode(
                DatatypeConverter.printDateTime(to), "UTF-8"));
      }
    } catch(UnsupportedEncodingException e) {
      // shouldn't happen
      throw new RuntimeException("JVM claims not to support UTF-8...", e);
    }

    return client.get(urlBuilder.toString(),
            new TypeReference<List<LogMessage>>() {
            });
  }

  /**
   * Retrieve the report files produced by this job. The actual results
   * of the job will be stored as a data bundle, available via the
   * {@link #resultBundle()} method.
   * 
   * @return a list of results, call <code>urlToDownload</code> on each
   *         result to get a (time-limited) URL from which the file can
   *         be downloaded.
   */
  public List<Downloadable> reports() {
    return client.get(url + "/reports", new TypeReference<List<Downloadable>>() {
    });
  }

  /**
   * Retrieve the data bundle containing this job's results.
   * 
   * @return a data bundle containing this job's results.
   */
  public DataBundle resultBundle() {
    if(resultBundle == null) {
      return null;
    } else {
      return client.get(resultBundle, new TypeReference<DataBundle>() {
      });
    }
  }

  /**
   * Refresh this job's data from the server to update things like the
   * {@link #progress} counter.
   */
  public void refresh() {
    client.getForUpdate(url, this);
  }

  /**
   * Delete this job, which also deletes any report files but not data
   * bundles created as a result of running the job - these must be
   * deleted independently.
   */
  public void delete() {
    client.delete(url);
  }

}
