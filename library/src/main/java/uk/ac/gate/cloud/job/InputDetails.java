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


/**
 * A single input specification for an annotation job. One job can (and
 * usually will) have many inputs.
 * 
 * @author Ian Roberts
 */
public class InputDetails extends InputSummary {

  /**
   * Character encoding to use when reading entries from the archive. If
   * <code>null</code>, the (W)ARC entry headers will be used to guess
   * an appropriate encoding for each entry (in the case of ARC or WARC
   * files) or a default of UTF-8 will be used (for other formats).
   */
  public String encoding;

  /**
   * MIME type to use when parsing entries from the archive. If
   * <code>null</code> the appropriate type will be guessed based on the
   * file name extension and (in the case of (W)ARC files) the HTTP
   * headers from the (W)ARC entry.
   */
  public String mimeTypeOverride;

  /**
   * Comma-separated list of file extensions that will be processed.
   * Entries that do not match any of these extensions will be ignored.
   * If <code>null</code> all entries that represent files (as opposed
   * to directories) will be processed. Should be left as
   * <code>null</code> for streaming input types, and will always be
   * <code>null</code> for (W)ARC inputs.
   */
  public String fileExtensions;

  /**
   * Space-separated list of MIME types used to filter the entries of
   * interest from ARC and WARC input files. Entries whose MIME type
   * does not match any of these will be ignored. Will be
   * <code>null</code> for non-(W)ARC inputs.
   */
  public String mimeTypes;

  /**
   * Refresh this input's state from the server.
   */
  public void refresh() {
    client.getForUpdate(url, this);
  }

  /**
   * Delete this input specification.
   */
  public void delete() {
    client.delete(url);
  }

}
