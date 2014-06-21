/*
 * RescheduleCommand.java
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

package thingynet.workflow.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import thingynet.workflow.Workflow;
import thingynet.workflow.WorkflowCommand;
import thingynet.workflow.WorkflowException;
import thingynet.workflow.WorkflowService;

public class RescheduleCommand implements WorkflowCommand {

    private final long delay;
    private final String node;
    @Value("${reschedule.command.timeout}")
    private long timeout;
    @Autowired
    private WorkflowService workflowService;

    public RescheduleCommand(long delay, String node) {
        this.delay = delay;
        this.node = node;
    }

    @Override
    public void execute(Workflow workflow) throws WorkflowException {
        workflow.setStart(workflow.getStart() + delay);
        workflow.setNode(node);
    }

    @Override
    public long getTimeout() {
        return timeout;
    }
}
