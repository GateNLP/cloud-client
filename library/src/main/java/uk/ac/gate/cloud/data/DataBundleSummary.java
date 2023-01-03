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
package uk.ac.gate.cloud.data;

import uk.ac.gate.cloud.common.ApiObject;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Summary information about a single data bundle.
 *
 * @author Ian Roberts
 */
public class DataBundleSummary extends ApiObject {

  /**
   * The job's numeric identifier.
   */
  public long id;

  /**
   * The name of the job.
   */
  public String name;

  /**
   * The bundle's detail URL.
   */
  public String url;

  /**
   * Can the contents of this bundle be directly downloaded?
   */
  public boolean downloadable;

  /**
   * Has this bundle been closed? Only non-closed bundles can accept
   * further file uploads.
   */
  public boolean closed;

  /**
   * Fetch the full details of this bundle.
   * 
   * @return a {@link DataBundle} object with the full bundle details.
   */
  public DataBundle details() {
    DataBundle details = client.get(url, new TypeReference<DataBundle>() {
    });
    details.url = this.url;
    return details;
  }

}
