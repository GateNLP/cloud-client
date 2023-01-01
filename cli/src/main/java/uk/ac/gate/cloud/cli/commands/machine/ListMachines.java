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
import uk.ac.gate.cloud.machine.MachineState;
import uk.ac.gate.cloud.machine.MachineSummary;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class ListMachines extends AbstractCommand {

  public void run(RestClient client, boolean jsonOutput, String... args) throws Exception {
    MachineManager mgr = new MachineManager(client);
    MachineState[] states = new MachineState[args.length];
    for(int i = 0; i < args.length; i++) {
      try {
        states[i] = MachineState.valueOf(args[i]);
      } catch(IllegalArgumentException e) {
        System.err.println("Invalid machine state " + args[i]);
        System.exit(1);
      }
    }
    List<MachineSummary> machines = mgr.listMachines(states);
    if(jsonOutput) {
      List<Machine> machineDetails = new ArrayList<>();
      if(machines != null) {
        for(MachineSummary summ : machines) {
          machineDetails.add(summ.details());
        }
      }
      mapper.writeValue(System.out, machineDetails);
    } else {
      if(machines == null || machines.isEmpty()) {
        System.out.println("No machines found");
      } else {
        // ID (6 cols), Name (32 cols), price (rest)
        System.out.println("    ID  Name                                      State");
        System.out.println("----------------------------------------------------------");
        Formatter f = new Formatter(System.out);
        for(MachineSummary summ : machines) {
          Machine m = summ.details();
          String name = m.name;
          if(name.length() > 40) {
            name = name.substring(0, 37) + "...";
          }
          f.format("%6d  %-40s  %s%n", m.id, name, m.state);
        }
        f.close();
      }
    }
  }

  @Override
  public void showHelp() throws Exception {
    System.err.println("Usage:");
    System.err.println();
    System.err.println("  list-machines [state1 state2 ...]");
    System.err.println();
    System.err.println("List all your reserved machines, optionally filtered by one");
    System.err.println("or more states:");
    System.err.println(" - inactive - not currently running");
    System.err.println(" - pending - in the process of starting up");
    System.err.println(" - active - up and running");
    System.err.println(" - stopping - in the process of shutting down");
    System.err.println(" - destroyed - deleted");
  }
}
