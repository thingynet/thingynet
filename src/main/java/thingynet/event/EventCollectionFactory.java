/*
 * EventCollectionFactory.java
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

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventCollectionFactory {

    @Autowired
    MongoClient mongoClient;

    public MongoCollection create(String db, String name, long size) {
        DB eventDb = mongoClient.getDB(db);
        if (!eventDb.collectionExists(name)) {
            DBObject options = BasicDBObjectBuilder.start().add("capped", true).add("size", size).get();
            eventDb.createCollection(name, options);
        }
        return new Jongo(eventDb).getCollection(name);
    }
}
