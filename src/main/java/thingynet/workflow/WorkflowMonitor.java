/*
 * WorkflowMonitor.java
 *
 * Copyright 2014 Jason Crossley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package thingynet.workflow;

import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static thingynet.workflow.WorkflowStatus.PROCESSING;
import static thingynet.workflow.WorkflowStatus.WAITING;

@Service
public class WorkflowMonitor implements Runnable {
    private final MongoCollection workflowCollection;
    private final Map<String, WorkflowCommand> commands;

    @Value("${workflow.monitor.sleep}")
    private long workflowMonitorSleep;

    @Value("${workflow.monitor.default.timeout}")
    private long defaultTimeout;

    @Autowired
    public WorkflowMonitor(WorkflowCommandFactory workflowCommandFactory, MongoCollection workflowCollection) {
        this.workflowCollection = workflowCollection;
        this.commands = workflowCommandFactory.getRegisteredCommands();
    }

    @Override
    public void run() {
        if (commands != null && !commands.isEmpty()) {
            try {
                while (true) {
                    commands.keySet().forEach(this::timeout);
                    sleep(workflowMonitorSleep);
                }
            } catch (InterruptedException ie) {
                // ignored
            }
        }
    }

    void timeout(String node) {
        WorkflowCommand command = commands.get(node);
        if (command != null) {
            long millis = currentTimeMillis();
            long timeout = millis - (command.getTimeout() > 0 ? command.getTimeout() : defaultTimeout);
            workflowCollection.update("{node:#, status:#, updated:{$lte:#}}", node, PROCESSING, timeout)
                    .with("{'$inc':{retry:1}, '$set':{status:#, updated:#}}", WAITING, millis);
        }
    }

}
