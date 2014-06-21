/*
 * PersistentEventHandlerTest.java
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

import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import thingynet.Application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static thingynet.event.PersistentEventHandler.LastEvent;
import static thingynet.event.TestEventCommand.TEST_EVENT_COMMAND;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class PersistentEventHandlerTest {
    private static final String KEY = "test.event";

    @Autowired
    OnEventSyncStrategy onEventSyncStrategy;

    @Autowired
    TestEventCommand testEventCommand;

    @Autowired
    private MongoCollection lastEventCollection;

    private PersistentEventHandler persistentEventHandler;
    private Event event;

    @Before
    public void before() {
        persistentEventHandler = new PersistentEventHandler(onEventSyncStrategy, lastEventCollection, KEY);
        event = new Event(new ObjectId(), TEST_EVENT_COMMAND, null);
        testEventCommand.reset();
        lastEventCollection.remove();
    }

    @Test
    public void getLastShouldReturnNullWhenFirstCreated() {
        assertThat(lastEventCollection.count(), is(0l));
        assertThat(persistentEventHandler.getLast(), nullValue());
    }

    @Test
    public void getLastShouldReturnLastWhenInRepository() {
        assertThat(lastEventCollection.count(), is(0l));
        persistentEventHandler.onEvent(event);
        assertThat(lastEventCollection.count(), is(1l));

        assertThat(persistentEventHandler.getLast(), is(event.getId()));
    }

    @Test
    public void onEventShouldDelegateToEventCommand() {
        persistentEventHandler.onEvent(event);
        assertThat(testEventCommand.getEvent(), is(event));
    }

    @Test
    public void onEventShouldInsertLastWhenItDoesNotAlreadyExist() {
        assertThat(lastEventCollection.count(), is(0l));
        persistentEventHandler.onEvent(event);

        assertThat(lastEventCollection.count(), is(1l));
        LastEvent lastEvent = lastEventCollection.findOne("{key:#}", KEY).as(LastEvent.class);
        assertThat(lastEvent.getLast(), is(event.getId()));
    }

    @Test
    public void onEventShouldUpdateLastWhenItAlreadyExist() {
        assertThat(lastEventCollection.count(), is(0l));
        persistentEventHandler.onEvent(event);
        assertThat(lastEventCollection.count(), is(1l));

        Event another = new Event(new ObjectId(), TEST_EVENT_COMMAND, null);
        persistentEventHandler.onEvent(another);

        LastEvent lastEvent = lastEventCollection.findOne("{key:#}", KEY).as(LastEvent.class);
        assertThat(lastEvent.getLast(), is(another.getId()));
    }

    @Test
    public void onEventShouldSetLastToEventId() {
        persistentEventHandler.onEvent(event);
        assertThat(persistentEventHandler.getLast(), is(event.getId()));
    }

}
