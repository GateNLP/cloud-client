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
package uk.ac.gate.cloud.cli.commands.machine;

import uk.ac.gate.cloud.machine.Machine;

public class StartMachine extends MachineControlCommand {

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  start-machine <machineid>");
    System.err.println();
    System.err.println("Start the machine with the given ID.  The command will");
    System.err.println("fail if the machine is not currently \"inactive\"");
  }

  @Override
  protected void controlMachine(Machine m) {
    m.start();
  }

}
