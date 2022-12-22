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

import uk.ac.gate.cloud.cli.commands.AbstractCommand;
import uk.ac.gate.cloud.client.RestClient;
import uk.ac.gate.cloud.machine.Machine;
import uk.ac.gate.cloud.machine.MachineManager;

public abstract class MachineControlCommand extends AbstractCommand {

  public void run(RestClient client, String... args) throws Exception {
    if(args.length < 1) {
      System.err.println("Usage: " + commandName() + " <machineid>");
      System.exit(1);
    }
    long machineId = -1;
    try {
      machineId = Long.parseLong(args[0]);
    } catch(NumberFormatException e) {
      System.err.println("Machine ID must be a valid number");
      System.exit(1);
    }

    MachineManager mgr = new MachineManager(client);
    Machine machine = mgr.getMachine(machineId);
    controlMachine(machine);
  }

  protected abstract String commandName();
  
  protected abstract void controlMachine(Machine j);
}
