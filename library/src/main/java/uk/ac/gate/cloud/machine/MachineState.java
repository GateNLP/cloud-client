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

/**
 * Enumeration of valid states for a GATE Cloud machine.
 */
public enum MachineState {

  /**
   * An instance is in the process of being started for this reservation
   */
  pending,

  /**
   * This machine is running, and incurring hourly charges.
   */
  active,

  /**
   * A request was made to stop this machine, but the
   * action of stopping it has not yet completed
   */
  stopping,

  /**
   * The machine is not currently running but could be started.
   */
  inactive,

  /**
   * The machine has been destroyed and the record is only retained for
   * reference/audit purposes.
   */
  destroyed;
}
