/*
 * OnEventSyncStrategyTest.java
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class OnEventSyncStrategyTest {
    @Mock
    private EventCommandFactory factory;

    @Mock
    private Event event;

    @Mock
    private EventCommand eventCommand;

    private OnEventSyncStrategy onEventSyncStrategy;

    @Before
    public void before() {
        onEventSyncStrategy = new OnEventSyncStrategy(factory);
    }

    @Test
    public void handleShouldExecuteCommandFromEventCommandFactory() {
        given(factory.getCommand(anyString())).willReturn(eventCommand);

        onEventSyncStrategy.handle(event);

        verify(factory).getCommand(anyString());
        verify(eventCommand).execute(event);
    }

    @Test
    public void handleShouldDoNothingWhenNullCommandReturnedFromEventCommandFactory() {
        given(factory.getCommand(anyString())).willReturn(null);

        onEventSyncStrategy.handle(event);

        verify(factory).getCommand(anyString());
        verifyNoMoreInteractions(eventCommand);
    }
}
