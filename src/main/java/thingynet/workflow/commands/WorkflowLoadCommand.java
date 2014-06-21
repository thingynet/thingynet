/*
 * WorkflowLoadCommand.java
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

import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import thingynet.workflow.Workflow;
import thingynet.workflow.WorkflowCommand;
import thingynet.workflow.WorkflowException;

import static java.lang.System.currentTimeMillis;
import static thingynet.workflow.WorkflowStatus.*;

@Component
public class WorkflowLoadCommand implements WorkflowCommand {

    @Autowired
    private MongoCollection workflowCollection;

    @Autowired
    private MongoCollection workflowLoadCollection;

    @Override
    public void execute(Workflow workflow) throws WorkflowException {
        long now = currentTimeMillis();
        WorkflowLoad log = new WorkflowLoad(
                workflowCollection.count("{status:#}", INITIALISING),
                workflowCollection.count("{status:#, start:{$lte:#}, dependencies:{$size:0}}", WAITING, now),
                workflowCollection.count("{status:#, start:{$lte:#}, dependencies:{$not: {$size:0}}}", WAITING, now),
                workflowCollection.count("{status:#, start:{$gt:#}, dependencies:{$size:0}}", WAITING, now),
                workflowCollection.count("{status:#}", PROCESSING)
        );
        workflowLoadCollection.save(log);
    }

    @Override
    public long getTimeout() {
        return 0;
    }
}
