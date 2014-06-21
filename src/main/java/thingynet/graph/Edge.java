/*
 * Edge.java
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

package thingynet.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import thingynet.value.Value;

import java.util.HashSet;
import java.util.Set;

public class Edge {

    @JsonProperty("_id")
    private String eid;
    private String from;
    private String to;
    private String label;
    private Set<String> traits = new HashSet<>();
    private long since;
    private long until;
    private double weight;
    private Value payload;
    private long updated;

    Edge() {
    }

    Edge(Vertex from, String label, Vertex to, Set<String> traits, long since, long until, double weight, Value payload) {
        this.eid = from.getType() + '§' + label + '§' + to.getType() + '§' + from.getName() + '§' + to.getName();
        this.from = from.getVid();
        this.label = label;
        this.to = to.getVid();
        if (traits != null) {
            this.traits.addAll(traits);
        }
        this.weight = weight;
        this.since = since;
        this.until = until;
        this.payload = payload;
    }

    public String getEid() {
        return eid;
    }

    public String getFrom() {
        return from;
    }

    public String getLabel() {
        return label;
    }

    public String getTo() {
        return to;
    }

    public Set<String> getTraits() {
        return traits;
    }

    public long getSince() {
        return since;
    }

    public void setSince(long since) {
        this.since = since;
    }

    public long getUntil() {
        return until;
    }

    public void setUntil(long until) {
        this.until = until;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Value getPayload() {
        return payload;
    }

    public void setPayload(Value payload) {
        this.payload = payload;
    }

    public long getUpdated() {
        return this.updated;
    }

    void setUpdated(long updated) {
        this.updated = updated;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
