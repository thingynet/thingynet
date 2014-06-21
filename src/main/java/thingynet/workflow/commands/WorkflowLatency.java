/*
 * WorkflowLatency.java
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

import thingynet.workflow.Workflow;
import thingynet.workflow.WorkflowLog;

public class WorkflowLatency extends WorkflowLog {
    private long latency;

    public WorkflowLatency() {
    }

    public WorkflowLatency(Workflow workflow) {
        super(workflow);
        latency = workflow.getUpdated() - workflow.getStart();
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }
}
