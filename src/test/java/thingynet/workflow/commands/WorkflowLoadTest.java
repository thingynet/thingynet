/*
 * WorkflowLoadTest.java
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

package thingynet.workflow.commands;

import org.jongo.MongoCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import thingynet.Application;
import thingynet.workflow.Workflow;
import thingynet.workflow.WorkflowException;
import thingynet.workflow.WorkflowService;

import static java.lang.System.currentTimeMillis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static thingynet.workflow.WorkflowStatus.PROCESSING;
import static thingynet.workflow.WorkflowStatus.WAITING;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class WorkflowLoadTest {
    private static final int TEN_SECONDS = 10000;

    @Autowired
    private MongoCollection workflowLoadCollection;

    @Autowired
    private MongoCollection workflowCollection;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private WorkflowLoadCommand command;

    @Before
    public void before() {
        workflowCollection.remove();
        workflowLoadCollection.remove();
    }

    @Test
    public void monitorLoadShouldRecordCorrectWorkflowCounts() throws WorkflowException {
        long start = currentTimeMillis();

        long count = workflowLoadCollection.count();
        assertThat(count, is(0l));

        Workflow dependency = workflowService.createOnHoldNow(null, null, null);
        Workflow workflow = workflowService.createWorkflow(null, null, start, null, WAITING);
        workflow.getDependencies().add(dependency.getId());
        workflowService.saveChanges(workflow);
        workflowService.createWorkflow(null, null, start, null, WAITING);
        workflowService.createWorkflow(null, null, start + TEN_SECONDS, null, WAITING);
        workflowService.createWorkflow(null, null, start, null, PROCESSING);

        command.execute(null);

        Iterable<WorkflowLoad> loads = workflowLoadCollection.find().as(WorkflowLoad.class);
        assertThat(loads, notNullValue());
        WorkflowLoad workflowLoad = loads.iterator().next();
        assertThat(workflowLoad, notNullValue());

        assertThat(workflowLoad.getCreated(), greaterThanOrEqualTo(start));
        assertThat(workflowLoad.getCreated(), lessThanOrEqualTo(currentTimeMillis()));
        assertThat(workflowLoad.getInitialising(), is(1l));
        assertThat(workflowLoad.getProcessing(), is(1l));
        assertThat(workflowLoad.getWaitingFutureStart(), is(1l));
        assertThat(workflowLoad.getWaitingNoDependencies(), is(1l));
        assertThat(workflowLoad.getWaitingWithDependencies(), is(1l));
    }

}
