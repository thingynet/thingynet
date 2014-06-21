/*
 * CacheConfig.java
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

package thingynet.cache;

import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import thingynet.event.*;

import java.io.IOException;

@Configuration
class CacheConfig {

    @Autowired
    MongoClient mongoClient;
    @Autowired
    EventCollectionFactory eventCollectionFactory;
    @Autowired
    OnEventAsyncStrategy onEventAsyncStrategy;
    @Value("${cache.db}")
    private String cacheDb;
    @Value("${cache.event.db}")
    private String cacheEventDb;
    @Value("${cache.event.queue.name}")
    private String cacheEventQueueName;
    @Value("${cache.event.queue.size}")
    private long cacheEventQueueSize;
    @Value("${cache.event.queue.poll}")
    private long cacheEventQueuePoll;

    @Bean
    public MongoCollection cacheCollection() throws IOException {
        Jongo jongo = new Jongo(mongoClient.getDB(cacheDb));
        MongoCollection collection = jongo.getCollection(thingynet.value.Value.class.getSimpleName());
        collection.ensureIndex("{'cacheKey': 1}");
        return collection;
    }

    @Bean
    public MongoCollection cacheEventCollection() throws IOException {
        return eventCollectionFactory.create(cacheEventDb, cacheEventQueueName, cacheEventQueueSize);
    }

    @Bean
    public EventListener cacheEventListener() throws IOException {
        return new EventListener(cacheEventCollection(), cacheEventHandler(), cacheEventQueuePoll);
    }

    @Bean
    public EventHandler cacheEventHandler() {
        return new TransientEventHandler(onEventAsyncStrategy);
    }
}
