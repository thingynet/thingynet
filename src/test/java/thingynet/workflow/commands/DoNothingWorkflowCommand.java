/*
 * DoNothingWorkflowCommand.java
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

import org.apache.log4j.Logger;
import thingynet.workflow.Workflow;
import thingynet.workflow.WorkflowCommand;
import thingynet.workflow.WorkflowException;

public class DoNothingWorkflowCommand implements WorkflowCommand {
    private static final Logger log = Logger.getLogger(DoNothingWorkflowCommand.class.getName());

    @Override
    public void execute(Workflow workflow) throws WorkflowException {
        log.debug("Do Nothing command executed");
    }

    @Override
    public long getTimeout() {
        return 0;
    }
}
