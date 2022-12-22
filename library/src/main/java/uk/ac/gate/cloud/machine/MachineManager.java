/*
 * Copyright (c) 2022 The University of Sheffield
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
package uk.ac.gate.cloud.machine;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.ac.gate.cloud.client.RestClient;
import uk.ac.gate.cloud.client.RestClientException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Main entry point for the machine management API, to start, stop and
 * monitor your cloud machines.
 */
public class MachineManager {

  private RestClient client;

  /**
   * Construct a <code>MachineManager</code> using the given
   * {@link RestClient} to communicate with the API.
   *
   * @param client the client object used for communication
   */
  public MachineManager(RestClient client) {
    this.client = client;
  }

  /**
   * Construct a <code>MachineManager</code> accessing the GATE Cloud
   * public API with the given credentials.
   *
   * @param apiKeyId API key ID for authentication
   * @param apiPassword corresponding password
   */
  public MachineManager(String apiKeyId, String apiPassword) {
    this(new RestClient(apiKeyId, apiPassword));
  }

  /**
   * List all the machines owned by the authenticated user that are in the
   * specified states.
   *
   * @param states states of interest. If not specified, the default is
   *          to return inactive, pending, active and stopping machines.
   * @return list of {@link MachineSummary} objects representing the
   *         matching machines - call the <code>details</code> method to get
   *         the full detail (which requires another API call).
   */
  public List<MachineSummary> listMachines(MachineState... states)
          throws RestClientException {
    StringBuilder urlBuilder = new StringBuilder("machine");
    if(states != null && states.length > 0) {
      try {
        urlBuilder.append("?state=");
        urlBuilder.append(URLEncoder.encode(states[0].name(), "UTF-8"));
        for(int i = 1; i < states.length; i++) {
          urlBuilder.append("&state=");
          urlBuilder.append(URLEncoder.encode(states[i].name(), "UTF-8"));
        }
      } catch(UnsupportedEncodingException e) {
        // shouldn't happen
        throw new RuntimeException("JVM claims not to support UTF-8...", e);
      }
    }
    return client.get(urlBuilder.toString(),
            new TypeReference<List<MachineSummary>>() {
            });
  }

  /**
   * Get an individual job by ID.
   *
   * @param id the ID of the job
   * @return the details of the requested job.
   */
  public Machine getMachine(long id) throws RestClientException {
    Machine m = client.get("machine/" + id, new TypeReference<Machine>() {
    });
    m.url = client.getBaseUrl().toString() + "machine/" + id;
    return m;
  }

}
