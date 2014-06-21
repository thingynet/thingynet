/*
 * WorkflowServiceTest.java
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

import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import thingynet.Application;
import thingynet.value.StringValue;

import java.util.Iterator;

import static java.lang.System.currentTimeMillis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static thingynet.workflow.WorkflowService.*;
import static thingynet.workflow.WorkflowStatus.*;
import static thingynet.workflow.commands.ExceptionWorkflowCommand.ERR_TEST;
import static thingynet.workflow.commands.ExceptionWorkflowCommand.WORKFLOW_COMMAND_THREW_EXCEPTION;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class WorkflowServiceTest {

    public static final String FIRST_WORKFLOW_COMMAND = "firstWorkflowCommand";
    private static final String TEST_WORKFLOW = "Test Workflow";
    private static final String NO_COMMAND = "noCommand";
    private static final String NEXT_WORKFLOW_COMMAND = "nextWorkflowCommand";
    private static final String DIVERT_WORKFLOW_COMMAND = "divertWorkflowCommand";
    private static final String EXCEPTION_WORKFLOW_COMMAND = "exceptionWorkflowCommand";
    private static final String DEPENDENCY_COMMAND = "dependencyCommand";

    private static final int TEN_SECONDS = 10000;
    private static final StringValue STRING_VALUE = new StringValue("String Value");

    @Autowired
    private MongoCollection workflowNodeCollection;

    @Autowired
    private MongoCollection workflowCollection;

    @Autowired
    private MongoCollection workflowLogCollection;

    @Autowired
    private WorkflowService workflowService;

    private WorkflowNode nextWorkflowNode;
    private WorkflowNode dependencyWorkflowNode;
    private long start;

    @Before
    public void before() {
        workflowCollection.remove();
        workflowNodeCollection.remove();
        workflowLogCollection.remove();

        WorkflowNode firstWorkflowNode = new WorkflowNode(FIRST_WORKFLOW_COMMAND, NEXT_WORKFLOW_COMMAND);
        workflowNodeCollection.save(firstWorkflowNode);

        nextWorkflowNode = new WorkflowNode(NEXT_WORKFLOW_COMMAND, null);
        workflowNodeCollection.save(nextWorkflowNode);

        dependencyWorkflowNode = new WorkflowNode(DEPENDENCY_COMMAND, null);
        workflowNodeCollection.save(dependencyWorkflowNode);

        start = currentTimeMillis();
    }

    @Test
    public void createWorkflowShouldReturnWorkflowWithExpectedValues() {
        Workflow original = workflowService.createWorkflow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, start, STRING_VALUE, INITIALISING);

        assertThat(original.getNode(), is(FIRST_WORKFLOW_COMMAND));
        assertThat(original.getStatus(), is(INITIALISING));
        assertThat(original.getUpdated(), greaterThanOrEqualTo(start));
        assertThat(original.getStart(), greaterThanOrEqualTo(start));
        assertThat(original.getRetry(), is(0));
        StringValue stringValue = (StringValue) original.getContext();
        assertThat(stringValue.getValue(), is(STRING_VALUE.getValue()));
    }

    @Test
    public void createWorkflowShouldSaveWorkflowWithExpectedValues() {
        Workflow original = workflowService.createWorkflow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, start, STRING_VALUE, INITIALISING);

        Workflow inserted = workflowCollection.findOne(original.getId()).as(Workflow.class);
        assertThat(inserted.getNode(), is(FIRST_WORKFLOW_COMMAND));
        assertThat(inserted.getStatus(), is(INITIALISING));
        assertThat(inserted.getUpdated(), greaterThanOrEqualTo(start));
        assertThat(inserted.getStart(), greaterThanOrEqualTo(start));
        assertThat(original.getRetry(), is(0));
        StringValue stringValue = (StringValue) inserted.getContext();
        assertThat(stringValue.getValue(), is(STRING_VALUE.getValue()));
    }

    @Test
    public void createReadyNowShouldSaveWorkflowWithWaitingStatusAndStartsNow() {
        Workflow original = workflowService.createReadyNow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, STRING_VALUE);

        Workflow inserted = workflowCollection.findOne(original.getId()).as(Workflow.class);
        assertThat(inserted.getNode(), is(FIRST_WORKFLOW_COMMAND));
        assertThat(inserted.getStatus(), is(WAITING));
        assertThat(inserted.getUpdated(), greaterThanOrEqualTo(start));
        assertThat(inserted.getStart(), greaterThanOrEqualTo(start));
        assertThat(inserted.getStart(), lessThanOrEqualTo(currentTimeMillis()));
        assertThat(inserted.getRetry(), is(0));
    }

    @Test
    public void createReadyScheduledShouldSaveWorkflowWithWaitingStatusAndScheduledStart() {
        long scheduledStart = currentTimeMillis() + TEN_SECONDS;
        Workflow original = workflowService.createReadyScheduled(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, STRING_VALUE, scheduledStart);

        Workflow inserted = workflowCollection.findOne(original.getId()).as(Workflow.class);
        assertThat(inserted.getNode(), is(FIRST_WORKFLOW_COMMAND));
        assertThat(inserted.getStatus(), is(WAITING));
        assertThat(inserted.getUpdated(), greaterThanOrEqualTo(start));
        assertThat(inserted.getStart(), is(scheduledStart));
        assertThat(inserted.getRetry(), is(0));
    }

    @Test
    public void createOnHoldNowShouldSaveWorkflowWithInitialisingStatusAndStartNow() {
        Workflow dependency = workflowService.createOnHoldNow(TEST_WORKFLOW, DEPENDENCY_COMMAND, STRING_VALUE);

        Workflow inserted = workflowCollection.findOne(dependency.getId()).as(Workflow.class);
        assertThat(inserted.getNode(), is(dependencyWorkflowNode.getName()));
        assertThat(inserted.getStatus(), is(INITIALISING));
        assertThat(inserted.getUpdated(), greaterThanOrEqualTo(start));
        assertThat(inserted.getStart(), greaterThanOrEqualTo(start));
        assertThat(inserted.getStart(), lessThanOrEqualTo(currentTimeMillis()));
        assertThat(inserted.getRetry(), is(0));
    }

    @Test
    public void createOnHoldScheduledShouldSaveWorkflowWithWaitingStateAndScheduledStart() {
        long scheduledStart = currentTimeMillis() + TEN_SECONDS;
        Workflow dependency = workflowService.createOnHoldScheduled(TEST_WORKFLOW, DEPENDENCY_COMMAND, STRING_VALUE, scheduledStart);

        Workflow inserted = workflowCollection.findOne(dependency.getId()).as(Workflow.class);
        assertThat(inserted.getNode(), is(dependencyWorkflowNode.getName()));
        assertThat(inserted.getStatus(), is(INITIALISING));
        assertThat(inserted.getUpdated(), greaterThanOrEqualTo(start));
        assertThat(inserted.getStart(), is(scheduledStart));
        assertThat(inserted.getRetry(), is(0));
    }

    @Test
    public void saveChangesShouldAddExpectedValuesToWorkflow() {
        Workflow original = workflowService.createWorkflow(TEST_WORKFLOW, null, 0, null, INITIALISING);
        assertThat(original.getContext(), nullValue());
        assertThat(original.getDependencies().size(), is(0));
        assertThat(original.getStart(), is(0l));
        assertThat(original.getStatus(), is(INITIALISING));
        assertThat(original.getNode(), nullValue());
        assertThat(original.getRetry(), is(0));
        long updated = original.getUpdated();

        try {
            Thread.sleep(100l);
        } catch (InterruptedException e) {
            // ignored
        }

        original.setContext(STRING_VALUE);
        original.getDependencies().add(original.getId());
        original.setStart(start);
        original.setStatus(PROCESSING);
        original.setNode(FIRST_WORKFLOW_COMMAND);
        original.setRetry(1);

        workflowService.saveChanges(original);

        Workflow inserted = workflowCollection.findOne(original.getId()).as(Workflow.class);
        StringValue stringValue = (StringValue) inserted.getContext();
        assertThat(stringValue.getValue(), is(STRING_VALUE.getValue()));
        assertThat(inserted.getDependencies().contains(original.getId()), is(true));
        assertThat(inserted.getStart(), is(start));
        assertThat(inserted.getStatus(), is(PROCESSING));
        assertThat(inserted.getNode(), is(FIRST_WORKFLOW_COMMAND));
        assertThat(inserted.getUpdated(), greaterThan(updated));
        assertThat(inserted.getRetry(), is(1));
    }

    @Test
    public void activateDependenciesShouldChangeStatusOfDependenciesToWaiting() {
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, start, STRING_VALUE, PROCESSING);

        Workflow dependency = workflowService.createOnHoldNow(TEST_WORKFLOW, DEPENDENCY_COMMAND, STRING_VALUE);
        workflow.getDependencies().add(dependency.getId());
        dependency = workflowService.createOnHoldNow(TEST_WORKFLOW, DEPENDENCY_COMMAND, STRING_VALUE);
        workflow.getDependencies().add(dependency.getId());

        workflowService.saveChanges(workflow);

        Iterator<ObjectId> ids = workflow.getDependencies().iterator();
        while (ids.hasNext()) {
            dependency = workflowCollection.findOne(ids.next()).as(Workflow.class);
            assertThat(dependency.getStatus(), is(INITIALISING));
        }

        long updated = currentTimeMillis();
        workflowService.activateOnHoldDependencies(workflow);

        ids = workflow.getDependencies().iterator();
        while (ids.hasNext()) {
            dependency = workflowCollection.findOne(ids.next()).as(Workflow.class);
            assertThat(dependency.getStatus(), is(WAITING));
            assertThat(dependency.getUpdated(), greaterThanOrEqualTo(updated));
        }
    }

    @Test
    public void getWaitingShouldReturnWorkflowWithStartTimeInPastAndNoDependenciesAndProcessingStatus() {
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, start, STRING_VALUE, WAITING);

        Workflow waiting = workflowService.getWaiting();

        assertThat(waiting.getId(), is(workflow.getId()));
        assertThat(waiting.getStart(), lessThanOrEqualTo(currentTimeMillis()));
        assertThat(waiting.getStatus(), is(PROCESSING));
        assertThat(waiting.getUpdated(), greaterThanOrEqualTo(workflow.getUpdated()));
    }

    @Test
    public void getWaitingShouldReturnNullForWorkflowWithInitialisingStatus() {
        workflowService.createWorkflow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, start, STRING_VALUE, INITIALISING);

        Workflow waiting = workflowService.getWaiting();

        assertThat(waiting, nullValue());
    }

    @Test
    public void getWaitingShouldReturnNullForWorkflowWithProcessingStatus() {
        workflowService.createWorkflow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, start, STRING_VALUE, PROCESSING);

        Workflow waiting = workflowService.getWaiting();

        assertThat(waiting, nullValue());
    }

    @Test
    public void getWaitingShouldReturnNullForWorkflowWithDependencies() {
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, start, STRING_VALUE, WAITING);
        workflow.getDependencies().add(workflowService.createOnHoldNow(TEST_WORKFLOW, DEPENDENCY_COMMAND, STRING_VALUE).getId());
        workflowService.saveChanges(workflow);
        assertThat(workflow.getDependencies().size(), is(1));

        Workflow waiting = workflowService.getWaiting();

        assertThat(waiting, nullValue());
    }

    @Test
    public void getWaitingShouldReturnNullForWorkflowWithStartTimeInFuture() {
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, start + 500, STRING_VALUE, WAITING);

        Workflow waiting = workflowService.getWaiting();
        assertThat(waiting, nullValue());

        try {
            Thread.sleep(500l);
        } catch (InterruptedException e) {
            // ignored
        }

        waiting = workflowService.getWaiting();
        assertThat(waiting.getId(), is(workflow.getId()));
        assertThat(waiting.getUpdated(), greaterThanOrEqualTo(workflow.getUpdated()));
    }

    @Test
    public void processShouldRemoveWorkflowAndLogErrorWhenNodeIsNull() {
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, null, start, STRING_VALUE, PROCESSING);

        workflowService.process(workflow);

        Workflow gone = workflowCollection.findOne(workflow.getId()).as(Workflow.class);
        assertThat(gone, nullValue());

        Iterable<WorkflowError> errors = workflowLogCollection.find().as(WorkflowError.class);
        assertThat(errors, notNullValue());
        WorkflowError error = errors.iterator().next();
        assertThat(error, notNullValue());

        assertThat(error.getName(), is(TEST_WORKFLOW));
        assertThat(error.getCommand(), nullValue());
        assertThat(error.getCreated(), greaterThanOrEqualTo(error.getStart()));
        assertThat(error.getStart(), greaterThanOrEqualTo(start));
        assertThat(error.getUpdated(), is(workflow.getUpdated()));
        assertThat(error.getClassification(), is(ERR_NODE));
        assertThat(error.getMessage(), is(WORKFLOW_NODE_MISSING));
    }

    @Test
    public void processShouldRemoveWorkflowAndLogErrorWhenCurrentIsNull() {
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, null, start, STRING_VALUE, PROCESSING);

        workflowService.process(workflow);

        Workflow gone = workflowCollection.findOne(workflow.getId()).as(Workflow.class);
        assertThat(gone, nullValue());

        Iterable<WorkflowError> errors = workflowLogCollection.find().as(WorkflowError.class);
        assertThat(errors, notNullValue());
        WorkflowError error = errors.iterator().next();
        assertThat(error, notNullValue());

        assertThat(error.getName(), is(TEST_WORKFLOW));
        assertThat(error.getCommand(), nullValue());
        assertThat(error.getCreated(), greaterThanOrEqualTo(error.getStart()));
        assertThat(error.getStart(), is(workflow.getStart()));
        assertThat(error.getUpdated(), is(workflow.getUpdated()));
        assertThat(error.getClassification(), is(ERR_NODE));
        assertThat(error.getMessage(), is(WORKFLOW_NODE_MISSING));
    }

    @Test
    public void processShouldRemoveWorkflowAndLogErrorWhenCommandThrowsCommandException() {
        WorkflowNode exceptionWorkflowNode = new WorkflowNode(EXCEPTION_WORKFLOW_COMMAND, null);
        workflowNodeCollection.save(exceptionWorkflowNode);
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, EXCEPTION_WORKFLOW_COMMAND, start, STRING_VALUE, PROCESSING);

        workflowService.process(workflow);

        Workflow gone = workflowCollection.findOne(workflow.getId()).as(Workflow.class);
        assertThat(gone, nullValue());

        Iterable<WorkflowError> errors = workflowLogCollection.find().as(WorkflowError.class);
        assertThat(errors, notNullValue());
        WorkflowError error = errors.iterator().next();
        assertThat(error, notNullValue());

        assertThat(error.getName(), is(TEST_WORKFLOW));
        assertThat(error.getCommand(), is(EXCEPTION_WORKFLOW_COMMAND));
        assertThat(error.getCreated(), greaterThanOrEqualTo(error.getStart()));
        assertThat(error.getStart(), is(workflow.getStart()));
        assertThat(error.getUpdated(), is(workflow.getUpdated()));
        assertThat(error.getClassification(), is(ERR_TEST));
        assertThat(error.getMessage(), is(WORKFLOW_COMMAND_THREW_EXCEPTION));
    }

    @Test
    public void processShouldRemoveWorkflowAndLogErrorWhenCommandIsNull() {
        WorkflowNode noCommandWorkflowNode = new WorkflowNode(NO_COMMAND, null);
        workflowNodeCollection.save(noCommandWorkflowNode);
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, NO_COMMAND, start, STRING_VALUE, PROCESSING);

        workflowService.process(workflow);

        Workflow gone = workflowCollection.findOne(workflow.getId()).as(Workflow.class);
        assertThat(gone, nullValue());

        Iterable<WorkflowError> errors = workflowLogCollection.find().as(WorkflowError.class);
        assertThat(errors, notNullValue());
        WorkflowError error = errors.iterator().next();
        assertThat(error, notNullValue());
        assertThat(error.getName(), is(TEST_WORKFLOW));
        assertThat(error.getCommand(), is(NO_COMMAND));
        assertThat(error.getCreated(), greaterThanOrEqualTo(error.getStart()));
        assertThat(error.getStart(), is(workflow.getStart()));
        assertThat(error.getUpdated(), is(workflow.getUpdated()));
        assertThat(error.getClassification(), is(ERR_COMMAND));
        assertThat(error.getMessage(), is(WORKFLOW_COMMAND_FACTORY_RETURNED_NULL));
    }

    @Test
    public void processShouldSetNodeToNextNodeAndStatusToWaitingAndRetryToZeroWhenWorkflowHasNextNode() {
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, start, STRING_VALUE, PROCESSING);
        workflowService.process(workflow);

        Workflow processed = workflowCollection.findOne(workflow.getId()).as(Workflow.class);

        assertThat(processed.getStatus(), is(WAITING));
        assertThat(processed.getNode(), is(nextWorkflowNode.getName()));
        assertThat(processed.getRetry(), is(0));
        assertThat(processed.getUpdated(), greaterThanOrEqualTo(workflow.getUpdated()));
    }

    @Test
    public void processShouldDeleteWorkflowWhenWorkflowIsCompleted() {
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, NEXT_WORKFLOW_COMMAND, start, STRING_VALUE, PROCESSING);
        workflowService.process(workflow);

        Workflow processed = workflowCollection.findOne(workflow.getId()).as(Workflow.class);

        assertThat(processed, nullValue());
    }

    @Test
    public void processShouldDeleteEntriesFromDependentWorkflowsWhenWorkflowIsCompleted() {
        Workflow dependency = workflowService.createOnHoldNow(TEST_WORKFLOW, NEXT_WORKFLOW_COMMAND, STRING_VALUE);

        Workflow workflow1 = workflowService.createWorkflow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, start, STRING_VALUE, PROCESSING);
        workflow1.getDependencies().add(dependency.getId());
        workflowService.saveChanges(workflow1);

        Workflow workflow2 = workflowService.createWorkflow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, start, STRING_VALUE, PROCESSING);
        workflow2.getDependencies().add(dependency.getId());
        workflowService.saveChanges(workflow2);

        assertThat(workflow1.getDependencies().iterator().next(), is(dependency.getId()));
        assertThat(workflow2.getDependencies().iterator().next(), is(dependency.getId()));

        workflowService.process(dependency);

        Workflow updated = workflowCollection.findOne(workflow1.getId()).as(Workflow.class);
        assertThat(updated.getDependencies().size(), is(0));
        assertThat(updated.getUpdated(), greaterThanOrEqualTo(workflow1.getUpdated()));

        updated = workflowCollection.findOne(workflow2.getId()).as(Workflow.class);
        assertThat(updated.getDependencies().size(), is(0));
        assertThat(updated.getUpdated(), greaterThanOrEqualTo(workflow2.getUpdated()));
    }

    @Test
    public void processShouldExecuteNodeReturnedFromCommandExecution() {
        WorkflowNode divertWorkflowNode = new WorkflowNode(DIVERT_WORKFLOW_COMMAND, null);
        workflowNodeCollection.save(divertWorkflowNode);

        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, DIVERT_WORKFLOW_COMMAND, start, STRING_VALUE, PROCESSING);
        workflowService.process(workflow);

        Workflow processed = workflowCollection.findOne(workflow.getId()).as(Workflow.class);

        assertThat(processed.getStatus(), is(WAITING));
        assertThat(processed.getNode(), is(FIRST_WORKFLOW_COMMAND));
        assertThat(processed.getRetry(), is(0));
        assertThat(processed.getUpdated(), greaterThanOrEqualTo(workflow.getUpdated()));
    }

    @Test
    public void processShouldNotUpdateWorkflowWhenRetryDoesNotMatch() {
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, FIRST_WORKFLOW_COMMAND, start, STRING_VALUE, PROCESSING);
        workflow.setRetry(1);
        workflowService.process(workflow);

        Workflow processed = workflowCollection.findOne(workflow.getId()).as(Workflow.class);

        assertThat(processed.getStatus(), is(PROCESSING));
        assertThat(processed.getNode(), is(FIRST_WORKFLOW_COMMAND));
        assertThat(processed.getUpdated(), equalTo(workflow.getUpdated()));
        assertThat(processed.getRetry(), is(0));
    }

    @Test
    public void processShouldNotUpdateWorkflowWhenNodeDoesNotMatch() {
        Workflow workflow = workflowService.createWorkflow(TEST_WORKFLOW, NEXT_WORKFLOW_COMMAND, start, STRING_VALUE, PROCESSING);
        workflow.setNode(FIRST_WORKFLOW_COMMAND);
        workflowService.process(workflow);

        Workflow processed = workflowCollection.findOne(workflow.getId()).as(Workflow.class);

        assertThat(processed.getStatus(), is(PROCESSING));
        assertThat(processed.getNode(), is(nextWorkflowNode.getName()));
        assertThat(processed.getUpdated(), equalTo(workflow.getUpdated()));
    }

}
