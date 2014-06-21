/*
 * ExceptionWorkflowCommand.java
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

import org.springframework.stereotype.Component;
import thingynet.workflow.Workflow;
import thingynet.workflow.WorkflowCommand;
import thingynet.workflow.WorkflowException;

@Component
public class ExceptionWorkflowCommand implements WorkflowCommand {

    public static final String ERR_TEST = "ERR_TEST";
    public static final String WORKFLOW_COMMAND_THREW_EXCEPTION = "Workflow command threw exception";

    @Override
    public void execute(Workflow workflow) throws WorkflowException {
        throw new WorkflowException(ERR_TEST, WORKFLOW_COMMAND_THREW_EXCEPTION);
    }

    @Override
    public long getTimeout() {
        return 0;
    }

}
