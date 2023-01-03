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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URL;
import java.util.Calendar;

/**
 * Full details about a cloud machine, and methods to modify and
 * control the machine.
 */
public class Machine extends MachineSummary {

  /**
   * The name assigned to the machine.
   */
  public String name;

  /**
   * The kind of machine, e.g. "twitterCollector", "mimir" or "teamware"
   */
  public String kind;

  /**
   * Price per hour charged while the machine is running.
   */
  public double hourlyPrice;

  /**
   * Date and time of the last state transition.
   */
  public Calendar lastTransition;

  /**
   * Public URL at which the services on this machine can be accessed.
   * Only set if the machine is running.
   */
  public URL publicUrl;

  /**
   * Date and time when the machine was launched, if it is currently
   * running.
   */
  public Calendar launchTime;

  /**
   * <em>Original</em> admin password that was generated for this machine
   * when it was reserved - this may not be the current password if you
   * have chosen to change it yourself directly on the machine.
   */
  public String adminPassword;

  /**
   * Change the name of this machine.
   *
   * @param newName the new name
   */
  public void rename(String newName) {
    ObjectNode request = JsonNodeFactory.instance.objectNode();
    request.put("name", newName);
    client.postForUpdate(url, this, request);
  }

  /**
   * Refresh this machine's data from the server to update things like the
   * {@link #state state}.
   */
  public void refresh() {
    client.getForUpdate(url, this);
  }

  /**
   * Common implementation for machine control actions.
   */
  protected void control(String action) {
    ObjectNode request = JsonNodeFactory.instance.objectNode();
    request.put("action", action);
    client.post(url + "/control", new TypeReference<JsonNode>() {
    }, request);
    refresh();
  }

  /**
   * Start this machine. The machine must be in the
   * {@link MachineState#inactive inactive} state or this operation will fail.
   */
  public void start() {
    control("start");
  }

  /**
   * Stop this machine. The machine must be in the
   * {@link MachineState#active active} state or this operation will fail.
   */
  public void stop() {
    control("stop");
  }


}
