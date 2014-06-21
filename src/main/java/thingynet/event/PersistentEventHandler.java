/*
 * PersistentEventHandler.java
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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;

public class PersistentEventHandler implements EventHandler {
    private final OnEventStrategy strategy;
    private final MongoCollection lastEventCollection;
    private final String key;

    public PersistentEventHandler(OnEventStrategy onEventStrategy, MongoCollection lastEventCollection, String lastEventKey) {
        this.strategy = onEventStrategy;
        this.lastEventCollection = lastEventCollection;
        this.key = lastEventKey;
    }

    public ObjectId getLast() {
        LastEvent lastEvent = lastEventCollection.findOne("{key:#}", key).as(LastEvent.class);
        if (lastEvent != null) {
            return lastEvent.last;
        }
        return null;
    }

    public void onEvent(Event event) {
        strategy.handle(event);
        lastEventCollection.update("{key:#}", key).upsert().with("{key:#, last:#}", key, event.getId());
    }

    public void onStop() {
    }

    static class LastEvent {
        @JsonProperty("_id")
        private ObjectId id;

        private String key;
        private ObjectId last;

        ObjectId getLast() {
            return last;
        }
    }
}
