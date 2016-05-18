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


import uk.ac.gate.cloud.common.ApiObject;
import uk.ac.gate.cloud.common.InputType;
import uk.ac.gate.cloud.data.DataBundle;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Summary information about an input specification.
 * 
 * @author Ian Roberts
 */
public class InputSummary extends ApiObject {
  /**
   * Detail URL.
   */
  public String url;

  /**
   * The type of this input (ZIP, TAR, ARC, ARC_RECORDS, TWITTER_STREAM
   * or DATASIFT_STREAM)
   */
  public InputType type;

  /**
   * URL of the data bundle that acts as the source of this input
   * specification. If this property is non-null then all the other
   * details will be <code>null</code> as they are derived from the
   * bundle.
   */
  public String sourceBundle;

  /**
   * Fetch the full details of this input specification from the server.
   */
  public InputDetails details() {
    return client.get(url, new TypeReference<InputDetails>() {
    });
  }

  /**
   * Get the {@link DataBundle} that is the source of this input, or
   * <code>null</code> if this input is not derived from a bundle.
   */
  public DataBundle sourceBundle() {
    if(sourceBundle == null) {
      return null;
    } else {
      return client.get(sourceBundle, new TypeReference<DataBundle>() {
      });
    }
  }
}
