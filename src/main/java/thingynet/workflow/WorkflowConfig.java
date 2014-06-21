/*
 * WorkflowConfig.java
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

import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import thingynet.workflow.commands.WorkflowLoad;

import java.io.IOException;

@Configuration
class WorkflowConfig {

    @Autowired
    MongoClient mongoClient;
    @Value("${workflow.db}")
    private String workflowDb;
    @Value("${workflow.log.db}")
    private String workflowLogDb;
    @Value("${workflow.load.db}")
    private String workflowLoadDb;
    @Value("${workflow.node.db}")
    private String workflowNodeDb;

    @Bean
    public MongoCollection workflowCollection() throws IOException {
        Jongo jongo = new Jongo(mongoClient.getDB(workflowDb));
        MongoCollection collection = jongo.getCollection(Workflow.class.getSimpleName());
        collection.ensureIndex("{'status':1, 'start':1, 'dependencies':1}");
        collection.ensureIndex("{'_id':1, 'status':1}");
        collection.ensureIndex("{'dependencies':1}");
        collection.ensureIndex("{'node':1, 'status':1, 'updated':1}");
        return collection;
    }

    @Bean
    public MongoCollection workflowLogCollection() throws IOException {
        Jongo jongo = new Jongo(mongoClient.getDB(workflowLogDb));
        return jongo.getCollection(WorkflowLog.class.getSimpleName());
    }

    @Bean
    public MongoCollection workflowLoadCollection() throws IOException {
        Jongo jongo = new Jongo(mongoClient.getDB(workflowLoadDb));
        return jongo.getCollection(WorkflowLoad.class.getSimpleName());
    }

    @Bean
    public MongoCollection workflowNodeCollection() throws IOException {
        Jongo jongo = new Jongo(mongoClient.getDB(workflowNodeDb));
        return jongo.getCollection(WorkflowNode.class.getSimpleName());
    }
}
