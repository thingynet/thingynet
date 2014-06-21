/*
 * EventListener.java
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

import com.mongodb.*;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import thingynet.value.Value;

public class EventListener implements Runnable {
    private static final Logger log = Logger.getLogger(EventListener.class.getName());
    private final MongoCollection collection;
    private final EventHandler handler;
    private final long cursorPollMillis;
    private volatile boolean running = true;
    private volatile boolean stopped = false;
    private DBCursor cursor;

    public EventListener(MongoCollection eventCollection, EventHandler eventHandler, long eventCursorPollMillis) {
        this.collection = eventCollection;
        this.handler = eventHandler;
        this.cursorPollMillis = eventCursorPollMillis;
    }

    @Override
    public void run() {
        try {
            while (running) {
                processEvents();
            }
        } finally {
            stopped = true;
        }
    }

    public void stop() {
        running = false;
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }

        try {
            while (!stopped) {
                Thread.sleep(50l);
            }
        } catch (InterruptedException e) {
            // ignored
        }
        handler.onStop();
    }

    void processEvents() {
        refreshCursor();
        try {
            consumeEvents();
        } catch (MongoCursorNotFoundException e) {
            // cursor needs regenerating
        }
    }

    private void consumeEvents() {
        while (cursor != null && cursor.hasNext() && cursor.getCursorId() != 0 && running) {
            DBObject dbObj = cursor.next();
            Event event = new Event(
                    (ObjectId) dbObj.get("_id"),
                    (String) dbObj.get("name"),
                    (Value) dbObj.get("payload")
            );
            handler.onEvent(event);
        }
    }

    private void refreshCursor() {
        if (cursor != null) {
            cursor.close();
            if (cursorPollMillis > 0) {
                try {
                    Thread.sleep(cursorPollMillis);
                } catch (InterruptedException e) {
                    log.error("Thread was interrupted", e);
                }
            }
        }
        if (handler.getLast() == null) {
            cursor = collection.getDBCollection().find()
                    .addOption(Bytes.QUERYOPTION_TAILABLE)
                    .addOption(Bytes.QUERYOPTION_AWAITDATA);
        } else {
            DBObject queryObj = new BasicDBObject("_id", new BasicDBObject("$gt", handler.getLast()));
            cursor = collection.getDBCollection().find(queryObj)
                    .addOption(Bytes.QUERYOPTION_TAILABLE)
                    .addOption(Bytes.QUERYOPTION_AWAITDATA);
        }
    }

    DBCursor getCursor() {
        return cursor;
    }

    void setCursor(DBCursor cursor) {
        this.cursor = cursor;
    }
}
