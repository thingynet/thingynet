/*
 * WorkflowCommandFactory.java
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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
class WorkflowCommandFactory {
    private static final Logger log = Logger.getLogger(WorkflowCommandFactory.class.getName());

    private Map<String, WorkflowCommand> registeredCommands;

    public WorkflowCommand getCommand(String name) {
        if (registeredCommands != null) {
            return registeredCommands.get(name);
        }
        return null;
    }

    public Map<String, WorkflowCommand> getRegisteredCommands() {
        return registeredCommands;
    }

    @Autowired(required = false)
    public void setRegisteredCommands(Map<String, WorkflowCommand> commands) {
        this.registeredCommands = commands;
        if (commands != null) {
            for (String key : commands.keySet()) {
                log.info("Registered: " + key);
            }
        }
    }
}
