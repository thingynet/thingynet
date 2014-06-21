/*
 * EventListenerTest.java
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
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import thingynet.Application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static thingynet.event.TestEventCommand.TEST_EVENT_COMMAND;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class EventListenerTest {
    private static final String EVENT_NAME = Event.class.getSimpleName();
    @Autowired
    MongoClient mongoClient;
    @Mock
    DBCursor mockCursor;
    @Value("${event.db}")
    private String eventDb;
    @Value("${event.db.size}")
    private long eventDbSize;
    @Autowired
    private TestEventCommand testEventCommand;
    private TestEventHandler testEventHandler;
    private MongoCollection eventCollection;
    private EventListener eventListener;
    private Event event;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        DB eventDb = mongoClient.getDB("test-event");
        if (eventDb.collectionExists(EVENT_NAME)) {
            eventDb.getCollection(EVENT_NAME).drop();
        }
        DBObject options = BasicDBObjectBuilder.start().add("capped", true).add("size", 10000l).get();
        eventDb.createCollection(EVENT_NAME, options);
        eventCollection = new Jongo(eventDb).getCollection(EVENT_NAME);

        testEventHandler = new TestEventHandler();
        eventListener = new EventListener(eventCollection, testEventHandler, 250l);

        event = new Event(new ObjectId(), TEST_EVENT_COMMAND, null);
        testEventCommand.reset();
    }

    @Test
    public void stopShouldTerminateEventListenerThread() throws Exception {
        Thread eventListenerThread = new Thread(eventListener);
        eventListenerThread.start();
        assertThat(eventListenerThread.isAlive(), is(true));

        eventListener.stop();
        assertThat(eventListenerThread.isAlive(), is(false));
    }

    @Test
    public void stopShouldCallEventHandlerOnStop() throws Exception {
        Thread eventListenerThread = new Thread(eventListener);
        eventListenerThread.start();

        eventListener.stop();
        assertThat(testEventHandler.getCountOnStopCalled(), is(1));
    }

    @Test
    public void stopShouldCloseCursor() throws Exception {
        Thread eventListenerThread = new Thread(eventListener);
        eventListenerThread.start();
        eventListener.setCursor(mockCursor);

        eventListener.stop();
        verify(mockCursor).close();
    }

    @Test
    public void stopShouldNullCursor() throws Exception {
        Thread eventListenerThread = new Thread(eventListener);
        eventListenerThread.start();
        eventListener.setCursor(mockCursor);

        eventListener.stop();
        assertThat(eventListener.getCursor(), nullValue());
    }

    @Test
    public void processEventsShouldCallHandlerOnEventWithEventFromEventCollection() throws InterruptedException {
        eventCollection.save(event);
        Thread eventListenerThread = new Thread(eventListener);
        eventListenerThread.start();

        Thread.sleep(100l);

        assertThat(testEventHandler.getEvent().getId(), is(event.getId()));
        assertThat(testEventHandler.getEvent().getName(), is(event.getName()));
        assertThat(testEventHandler.getEvent().getPayload(), is(event.getPayload()));

        eventListener.stop();
    }

    @Test
    public void processEventsShouldCallHandlerOnEventOnceWhenOnlyOneEventExists() throws InterruptedException {
        eventCollection.save(event);
        Thread eventListenerThread = new Thread(eventListener);
        eventListenerThread.start();

        Thread.sleep(100l);

        assertThat(testEventHandler.getCountOnEventCalled(), is(1));
        assertThat(testEventHandler.getHistory().size(), is(1));
        assertThat(testEventHandler.getHistory().get(0).getId(), is(event.getId()));
        assertThat(testEventHandler.getHistory().get(0).getName(), is(event.getName()));
        assertThat(testEventHandler.getHistory().get(0).getPayload(), is(event.getPayload()));


        eventListener.stop();
    }

    @Test
    public void processEventsShouldCallHandlerOnEventInSameNumberOfTimesAndOrderAsEventsInEventCollection() throws InterruptedException {
        Event event2 = new Event(new ObjectId(), TEST_EVENT_COMMAND, null);
        Event event3 = new Event(new ObjectId(), TEST_EVENT_COMMAND, null);
        eventCollection.save(event);
        eventCollection.save(event2);
        eventCollection.save(event3);

        Thread eventListenerThread = new Thread(eventListener);
        eventListenerThread.start();

        Thread.sleep(100l);

        assertThat(testEventHandler.getCountOnEventCalled(), is(3));
        assertThat(testEventHandler.getHistory().size(), is(3));
        assertThat(testEventHandler.getHistory().get(0).getId(), is(event.getId()));
        assertThat(testEventHandler.getHistory().get(1).getId(), is(event2.getId()));
        assertThat(testEventHandler.getHistory().get(2).getId(), is(event3.getId()));

        eventListener.stop();
    }

    @Test
    public void processEventsShouldCallHandlerOnEventWhenNewEventSavedInEventCollection() throws InterruptedException {
        eventCollection.save(event);

        Thread eventListenerThread = new Thread(eventListener);
        eventListenerThread.start();

        Thread.sleep(100l);

        assertThat(testEventHandler.getCountOnEventCalled(), is(1));
        assertThat(testEventHandler.getHistory().size(), is(1));
        assertThat(testEventHandler.getHistory().get(0).getId(), is(event.getId()));

        Event event2 = new Event(new ObjectId(), TEST_EVENT_COMMAND, null);
        eventCollection.save(event2);

        Thread.sleep(100l);

        assertThat(testEventHandler.getCountOnEventCalled(), is(2));
        assertThat(testEventHandler.getHistory().size(), is(2));
        assertThat(testEventHandler.getHistory().get(0).getId(), is(event.getId()));
        assertThat(testEventHandler.getHistory().get(1).getId(), is(event2.getId()));

        eventListener.stop();
    }

    @Test
    public void processEventsShouldCallHandlerOnEventWhenEventListenerIsRestartedWithExistingEventHandlerThatTracksProcessedEvents() throws InterruptedException {
        eventCollection.save(event);

        Thread eventListenerThread = new Thread(eventListener);
        eventListenerThread.start();

        Thread.sleep(100l);

        assertThat(testEventHandler.getCountGetLastCalled(), is(1));
        assertThat(testEventHandler.getCountOnEventCalled(), is(1));
        assertThat(testEventHandler.getHistory().size(), is(1));
        assertThat(testEventHandler.getHistory().get(0).getId(), is(event.getId()));

        eventListener.stop();

        eventListener = new EventListener(eventCollection, testEventHandler, 250l);
        eventListenerThread = new Thread(eventListener);
        eventListenerThread.start();

        Event event2 = new Event(new ObjectId(), TEST_EVENT_COMMAND, null);
        eventCollection.save(event2);

        Thread.sleep(100l);

        assertThat(testEventHandler.getCountGetLastCalled(), is(3));
        assertThat(testEventHandler.getCountOnEventCalled(), is(2));
        assertThat(testEventHandler.getHistory().size(), is(2));
        assertThat(testEventHandler.getHistory().get(0).getId(), is(event.getId()));
        assertThat(testEventHandler.getHistory().get(1).getId(), is(event2.getId()));

        eventListener.stop();
    }
}
