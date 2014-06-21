/*
 * TestEventHandler.java
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

import java.util.ArrayList;
import java.util.List;

public class TestEventHandler implements EventHandler {

    private Event event;
    private ArrayList<Event> history = new ArrayList<>();
    private int countOnEventCalled;
    private int countOnStopCalled;
    private int countGetLastCalled;

    @Override
    public void onEvent(Event event) {
        countOnEventCalled++;
        this.event = event;
        history.add(event);
    }

    @Override
    public void onStop() {
        countOnStopCalled++;
    }

    @Override
    public ObjectId getLast() {
        countGetLastCalled++;
        return event == null ? null : event.getId();
    }

    public Event getEvent() {
        return event;
    }

    public List<Event> getHistory() {
        return history;
    }

    public int getCountOnEventCalled() {
        return countOnEventCalled;
    }

    public int getCountOnStopCalled() {
        return countOnStopCalled;
    }

    public int getCountGetLastCalled() {
        return countGetLastCalled;
    }
}
