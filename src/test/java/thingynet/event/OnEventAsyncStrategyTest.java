/*
 * OnEventAsyncStrategyTest.java
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import thingynet.Application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static thingynet.event.TestEventCommand.TEST_EVENT_COMMAND;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class OnEventAsyncStrategyTest {

    @Autowired
    private OnEventAsyncStrategy onEventAsyncStrategy;

    @Autowired
    private TestEventCommand testEventCommand;

    private Event event;

    @Before
    public void before() throws Exception {
        event = new Event(new ObjectId(), TEST_EVENT_COMMAND, null);
        testEventCommand.reset();
    }

    @Test
    public void handleShoudExecuteCommand() throws InterruptedException {
        onEventAsyncStrategy.handle(event);
        Thread.sleep(100l);

        assertThat(testEventCommand.getEvent().getId(), is(event.getId()));
    }
}
