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
package uk.ac.gate.cloud.common;

/**
 * Enumeration of valid input types.
 * 
 * @author Ian Roberts
 */
public enum InputType {
  /**
   * Archive file in the ZIP format.
   */
  ZIP,

  /**
   * Archive file in TAR format, possibly compressed with gzip or bzip2.
   */
  TAR,

  /**
   * Internet Archive ARC file.
   */
  ARC,
  
  /**
   * Internet Archive WARC file.
   */
  WARC,

  /**
   * JSON in the Twitter streaming format (a sequence of JSON objects,
   * one per Tweet, concatenated together with no separators).
   */
  TWITTER_STREAM,

  /**
   * JSON in the streaming-style format produced by DataSift (a sequence
   * of JSON objects, one per item, concatenated together). This format
   * works with the <a href=
   * "http://dev.datasift.com/docs/getting-started/data/interaction"
   * >interaction format</a>, which maps the common fields across
   * different social media sources into a common representation.
   */
  DATASIFT_STREAM,
}
