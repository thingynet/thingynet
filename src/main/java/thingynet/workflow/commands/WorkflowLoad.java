/*
 * WorkflowLoad.java
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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class WorkflowLoad {
    private long created;
    private long initialising;
    private long waitingNoDependencies;
    private long waitingWithDependencies;
    private long waitingFutureStart;
    private long processing;

    public WorkflowLoad() {
    }

    public WorkflowLoad(long initialising, long waitingNoDependencies, long waitingWithDependencies, long waitingFutureStart, long processing) {
        this.created = System.currentTimeMillis();
        this.initialising = initialising;
        this.waitingNoDependencies = waitingNoDependencies;
        this.waitingWithDependencies = waitingWithDependencies;
        this.waitingFutureStart = waitingFutureStart;
        this.processing = processing;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getInitialising() {
        return initialising;
    }

    public void setInitialising(long initialising) {
        this.initialising = initialising;
    }

    public long getWaitingNoDependencies() {
        return waitingNoDependencies;
    }

    public void setWaitingNoDependencies(long waitingNoDependencies) {
        this.waitingNoDependencies = waitingNoDependencies;
    }

    public long getWaitingWithDependencies() {
        return waitingWithDependencies;
    }

    public void setWaitingWithDependencies(long waitingWithDependencies) {
        this.waitingWithDependencies = waitingWithDependencies;
    }

    public long getWaitingFutureStart() {
        return waitingFutureStart;
    }

    public void setWaitingFutureStart(long waitingFutureStart) {
        this.waitingFutureStart = waitingFutureStart;
    }

    public long getProcessing() {
        return processing;
    }

    public void setProcessing(long processing) {
        this.processing = processing;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(null, SHORT_PREFIX_STYLE);
    }
}