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
import uk.ac.gate.cloud.common.ApiObject;
import uk.ac.gate.cloud.data.DataBundle;

/**
 * Summary information about a single cloud machine.
 *
 * @author Ian Roberts
 */
public class MachineSummary extends ApiObject {
  /**
   * The machine's numeric identifier.
   */
  public long id;

  /**
   * Current state of the machine.
   */
  public MachineState state;

  /**
   * The machine's detail URL.
   */
  public String url;

  /**
   * Fetch the full details of this machine.
   *
   * @return a {@link Machine} object with the full details.
   */
  public Machine details() {
    Machine details = client.get(url, new TypeReference<Machine>() {
    });
    details.url = this.url;
    return details;
  }

}
