/*
 * WorkflowLatencyTest.java
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

package thingynet.workflow;

import org.jongo.MongoCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import thingynet.Application;
import thingynet.workflow.commands.WorkflowLatency;
import thingynet.workflow.commands.WorkflowLatencyCommand;

import static java.lang.System.currentTimeMillis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class WorkflowLatencyTest {
    private static final long EXPECTED_LATENCY = 1000l;

    @Autowired
    private MongoCollection workflowLogCollection;

    @Autowired
    private WorkflowLatencyCommand command;

    @Before
    public void before() {
        workflowLogCollection.remove();
    }

    @Test
    public void excuteShouldRecordCorrectLatency() throws WorkflowException {
        long count = workflowLogCollection.count();
        assertThat(count, is(0l));

        Workflow workflow = new Workflow(null, null, null, currentTimeMillis(), null);
        workflow.setUpdated(workflow.getStart() + EXPECTED_LATENCY);

        command.execute(workflow);

        Iterable<WorkflowLatency> loads = workflowLogCollection.find().as(WorkflowLatency.class);
        assertThat(loads, notNullValue());
        WorkflowLatency workflowLatency = loads.iterator().next();
        assertThat(workflowLatency, notNullValue());
        assertThat(workflowLatency.getLatency(), is(EXPECTED_LATENCY));
    }

}
