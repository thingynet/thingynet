/*
 * WorkflowMonitorTest.java
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

import static java.lang.System.currentTimeMillis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static thingynet.workflow.WorkflowStatus.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class WorkflowMonitorTest {

    private static final String TEST_WORKFLOW = "Test Workflow";
    private static final String SHORT_TIMEOUT_WORKFLOW_COMMAND = "shortTimeoutWorkflowCommand";
    private static final String LONG_TIMEOUT_WORKFLOW_COMMAND = "longTimeoutWorkflowCommand";

    @Autowired
    private MongoCollection workflowCollection;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private WorkflowMonitor workflowMonitor;

    private long start;

    @Before
    public void before() {
        workflowCollection.remove();

        start = currentTimeMillis();
    }

    @Test
    public void timeoutShouldUpdateWorkflowWhenUpdatedLessThanOrEqualToNowMinusTimeout() throws InterruptedException {
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, SHORT_TIMEOUT_WORKFLOW_COMMAND, start, null, PROCESSING);
        Thread.sleep(10l);
        workflowMonitor.timeout(SHORT_TIMEOUT_WORKFLOW_COMMAND);

        Workflow processed = workflowCollection.findOne(workflow.getId()).as(Workflow.class);

        assertThat(processed.getStatus(), is(WAITING));
        assertThat(processed.getNode(), is(SHORT_TIMEOUT_WORKFLOW_COMMAND));
        assertThat(processed.getUpdated(), greaterThanOrEqualTo(workflow.getUpdated()));
        assertThat(processed.getRetry(), is(1));
    }

    @Test
    public void timeoutShouldNotUpdateWorkflowWhenUpdatedGreaterThanNowMinusTimeout() {
        WorkflowNode workflowNode = new WorkflowNode(LONG_TIMEOUT_WORKFLOW_COMMAND, null);
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, LONG_TIMEOUT_WORKFLOW_COMMAND, start, null, PROCESSING);
        workflowMonitor.timeout(LONG_TIMEOUT_WORKFLOW_COMMAND);

        Workflow processed = workflowCollection.findOne(workflow.getId()).as(Workflow.class);

        assertThat(processed.getStatus(), is(PROCESSING));
        assertThat(processed.getNode(), is(workflowNode.getName()));
        assertThat(processed.getUpdated(), is(workflow.getUpdated()));
        assertThat(processed.getRetry(), is(0));
    }

    @Test
    public void timeoutShouldNotUpdateWorkflowWhenStatusIsWaiting() {
        WorkflowNode workflowNode = new WorkflowNode(SHORT_TIMEOUT_WORKFLOW_COMMAND, null);
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, SHORT_TIMEOUT_WORKFLOW_COMMAND, start, null, WAITING);
        workflowMonitor.timeout(SHORT_TIMEOUT_WORKFLOW_COMMAND);

        Workflow processed = workflowCollection.findOne(workflow.getId()).as(Workflow.class);

        assertThat(processed.getStatus(), is(WAITING));
        assertThat(processed.getNode(), is(workflowNode.getName()));
        assertThat(processed.getUpdated(), is(workflow.getUpdated()));
        assertThat(processed.getRetry(), is(0));
    }

    @Test
    public void timeoutShouldNotUpdateWorkflowWhenStatusIsInitialising() {
        WorkflowNode workflowNode = new WorkflowNode(SHORT_TIMEOUT_WORKFLOW_COMMAND, null);
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, SHORT_TIMEOUT_WORKFLOW_COMMAND, start, null, INITIALISING);
        workflowMonitor.timeout(SHORT_TIMEOUT_WORKFLOW_COMMAND);

        Workflow processed = workflowCollection.findOne(workflow.getId()).as(Workflow.class);

        assertThat(processed.getStatus(), is(INITIALISING));
        assertThat(processed.getNode(), is(workflowNode.getName()));
        assertThat(processed.getUpdated(), is(workflow.getUpdated()));
        assertThat(processed.getRetry(), is(0));
    }
}
