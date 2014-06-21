/*
 * EdgeService.java
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
import static org.apache.commons.lang3.Validate.notNull;

@Service
public class EdgeService {

    @Autowired
    private MongoCollection edgeCollection;

    public Edge createEdge(Vertex from, String label, Vertex to, Set<String> traits, long since, long until, double weight, Value payload) {
        notNull(from);
        notNull(label);
        notNull(to);

        Edge edge = new Edge(from, label, to, traits, since, until, weight, payload);
        saveChanges(edge);
        return edge;
    }

    public void saveChanges(Edge edge) {
        notNull(edge);
        edge.setUpdated(currentTimeMillis());
        edgeCollection.save(edge);
    }

    public Edge getEdge(String eid) {
        return edgeCollection.findOne(
                "{_id:#}", eid)
                .as(Edge.class);
    }

    public Iterable<Edge> getEdgesFromVertex(Vertex vertex) {
        notNull(vertex);
        return edgeCollection.find(
                "{from:#}", vertex.getVid())
                .as(Edge.class);
    }

    public Iterable<Edge> getEdgesToVertex(Vertex vertex) {
        notNull(vertex);
        return edgeCollection.find(
                "{to:#}", vertex.getVid())
                .as(Edge.class);
    }

    public Iterable<Edge> getEdgesForArcLabel(String label) {
        notNull(label);
        return edgeCollection.find(
                "{label:#}", label)
                .as(Edge.class);
    }

    public Iterable<Edge> getEdgesForArcTypes(String fromType, String label, String toType) {
        notNull(label);
        return edgeCollection.find(
                "{_id:{$regex:#}}", "^" + fromType + "§" + toType)
                .as(Edge.class);
    }

}
