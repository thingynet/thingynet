/*
 * Vertex.java
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

public class Vertex {

    @JsonProperty("_id")
    private String vid;

    private String name;

    private String type;

    private Set<String> traits = new HashSet<>();

    private Value payload;

    private long updated;

    Vertex() {
    }

    Vertex(String name, String type, Set<String> traits, Value payload) {
        this.vid = type + '§' + name;
        this.name = name;
        this.type = type;
        this.traits = traits;
        this.payload = payload;
    }

    public String getVid() {
        return vid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Set<String> getTraits() {
        return traits;
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
