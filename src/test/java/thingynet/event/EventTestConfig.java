/*
 * EventTestConfig.java
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

package thingynet.event;

import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@PropertySource("/test.properties")
class EventTestConfig {

    @Autowired
    MongoClient mongoClient;

    @Autowired
    OnEventSyncStrategy onEventSyncStrategy;

    @Autowired
    String lastEventKey;

    @Bean
    public MongoCollection lastEventCollection() throws IOException {
        Jongo jongo = new Jongo(mongoClient.getDB("test-event"));
        return jongo.getCollection("lastEvent");
    }

    @Bean
    String lastEventKey() {
        return "testLastEvent";
    }

    @Bean
    ExecutorService eventExecutorService() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean
    PersistentEventHandler persistentEventHandler() throws IOException {
        return new PersistentEventHandler(onEventSyncStrategy, lastEventCollection(), lastEventKey);
    }

    @Bean
    @Scope("prototype")
    TransientEventHandler transientEventHandler() {
        return new TransientEventHandler(onEventSyncStrategy);
    }
}
