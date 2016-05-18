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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.output.ThresholdingOutputStream;

/**
 * Output stream that decides whether or not to GZIP compress the data
 * based on the total amount written. If the stream is closed before the
 * threshold is reached, then the data is sent as-is, otherwise the
 * "Content-Encoding" header is set on the connection and the data is
 * sent with GZIP compression.
 */
public class GZIPThresholdOutputStream extends ThresholdingOutputStream {

  private HttpURLConnection connection;

  private ByteArrayOutputStream buffer;

  private OutputStream currentStream;

  public GZIPThresholdOutputStream(HttpURLConnection connection, int threshold) {
    super(threshold);
    this.connection = connection;
    buffer = new ByteArrayOutputStream(threshold);
    // initially, we write to the buffer
    currentStream = buffer;
  }

  @Override
  protected OutputStream getStream() throws IOException {
    return currentStream;
  }

  @Override
  protected void thresholdReached() throws IOException {
    // add header
    connection.setRequestProperty("Content-Encoding", "gzip");
    // wrap output stream in GZIP
    currentStream = new GZIPOutputStream(connection.getOutputStream());
    // send buffered data
    currentStream.write(buffer.toByteArray());
    // from now on, we are writing directly to the connection
    buffer = null;
  }

  @Override
  public void close() throws IOException {
    if(buffer != null) {
      // we have finished writing data without hitting the threshold, so
      // send it uncompressed
      currentStream = connection.getOutputStream();
      currentStream.write(buffer.toByteArray());
    }
    super.close();
  }

}
