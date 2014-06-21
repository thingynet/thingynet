/*
 * VertexService.java
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

import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import thingynet.value.Value;

import java.util.Set;

import static java.lang.System.currentTimeMillis;

@Service
public class VertexService {

    @Autowired
    private MongoCollection vertexCollection;

    public Vertex createVertex(String name, String type, Set<String> traits, Value payload) {
        Vertex vertex = new Vertex(name, type, traits, payload);
        saveChanges(vertex);
        return vertex;
    }

    public void saveChanges(Vertex vertex) {
        vertex.setUpdated(currentTimeMillis());
        vertexCollection.save(vertex);
    }

    public Vertex getVertex(String vid) {
        return vertexCollection.findOne(
                "{_id:#}", vid)
                .as(Vertex.class);
    }

    public Iterable<Vertex> getVerticesForType(String type) {
        return vertexCollection.find(
                "{_id:{$regex:#}}", "^" + type + "§")
                .as(Vertex.class);
    }

    public Iterable<Vertex> getVerticesForTypeWithTraits(String type, Set<String> traits) {
        return vertexCollection.find(
                "{_id:{$regex:#}, traits:{$all:#}}", "^" + type + "§", traits)
                .as(Vertex.class);
    }

}
