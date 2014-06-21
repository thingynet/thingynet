/*
 * WorkflowLog.java
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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

@JsonTypeInfo(use = CLASS, property = "_class")
public class WorkflowLog {

    private String name;

    private String command;

    private long created;

    private long start;

    private long updated;

    public WorkflowLog() {
    }

    protected WorkflowLog(Workflow workflow) {
        name = workflow.getName();
        created = currentTimeMillis();
        start = workflow.getStart();
        updated = workflow.getUpdated();
        if (workflow.getNode() != null) {
            command = workflow.getNode();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(null, SHORT_PREFIX_STYLE);
    }
}