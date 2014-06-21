/*
 * GraphConfig.java
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

import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
class GraphConfig {

    @Autowired
    MongoClient mongoClient;
    @Value("${graph.db}")
    private String graphDb;

    @Bean
    public MongoCollection vertexCollection() throws IOException {
        Jongo jongo = new Jongo(mongoClient.getDB(graphDb));
        return jongo.getCollection(Vertex.class.getSimpleName());
    }

    @Bean
    public MongoCollection edgeCollection() throws IOException {
        Jongo jongo = new Jongo(mongoClient.getDB(graphDb));
        MongoCollection collection = jongo.getCollection(Edge.class.getSimpleName());
        collection.ensureIndex("{'vid':1}");
        collection.ensureIndex("{'label':1}");
        collection.ensureIndex("{'destination.vid':1}");
        return collection;
    }
}
