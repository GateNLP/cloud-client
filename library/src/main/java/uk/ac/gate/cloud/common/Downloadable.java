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

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents a file that can be downloaded. Call {@link #urlToDownload()}
 * to get a (time-limited) URL from which the actual download can be
 * obtained.
 * 
 * @author Ian Roberts
 */
public class Downloadable extends ApiObject {
  @JsonCreator
  public Downloadable(URL url) {
    this.url = url;
  }

  public URL url;

  /**
   * Generate a URL from which the file can be downloaded. This URL
   * should be accessed immediately as it is time-limited and will
   * expire if not used. If the URL has expired, simply call this method
   * again as a fresh URL is generated every time.
   * 
   * @return downloadable URL.
   */
  public URL urlToDownload() {
    return client.getRedirect(url);
  }
}
