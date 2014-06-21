/*
 * Workflow.java
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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bson.types.ObjectId;
import thingynet.value.Value;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class Workflow {

    @JsonProperty("_id")
    private ObjectId id;

    private String name;

    private String node;

    private WorkflowStatus status;

    private long start;

    private long updated;

    private int retry;

    private List<ObjectId> dependencies = new ArrayList<>();

    private Value context;

    Workflow() {
    }

    Workflow(String name, String node, WorkflowStatus status, long start, Value context) {
        this.name = name;
        this.node = node;
        this.status = status;
        this.start = start;
        this.context = context;
        this.updated = currentTimeMillis();
    }

    public ObjectId getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    WorkflowStatus getStatus() {
        return this.status;
    }

    void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    public long getStart() {
        return this.start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getUpdated() {
        return this.updated;
    }

    void setUpdated(long updated) {
        this.updated = updated;
    }

    public int getRetry() {
        return retry;
    }

    void setRetry(int retry) {
        this.retry = retry;
    }

    public List<ObjectId> getDependencies() {
        return this.dependencies;
    }

    public Value getContext() {
        return context;
    }

    public void setContext(Value context) {
        this.context = context;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
