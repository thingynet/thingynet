/*
 * WorkflowService.java
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import thingynet.value.Value;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static thingynet.workflow.WorkflowStatus.*;

@Service
public class WorkflowService implements Runnable {

    static final String WORKFLOW_COMMAND_FACTORY_RETURNED_NULL = "Workflow command factory returned null";
    static final String WORKFLOW_NODE_MISSING = "Workflow node missing";

    static final String ERR_NODE = "Workflow Vertex Error";
    static final String ERR_COMMAND = "Workflow Command Error";

    @org.springframework.beans.factory.annotation.Value("${workflow.service.sleep}")
    private long workflowServiceSleep;

    @Autowired
    private MongoCollection workflowCollection;

    @Autowired
    private MongoCollection workflowLogCollection;

    @Autowired
    private MongoCollection workflowNodeCollection;

    @Autowired
    private WorkflowCommandFactory workflowCommandFactory;

    public Workflow createWorkflow(String name, String node, long start, Value context, WorkflowStatus status) {
        Workflow workflow = new Workflow(name, node, status, start, context);
        workflowCollection.save(workflow);
        return workflow;
    }

    public Workflow createReadyNow(String name, String node, Value context) {
        return createWorkflow(name, node, currentTimeMillis(), context, WAITING);
    }

    public Workflow createReadyScheduled(String name, String node, Value context, long start) {
        return createWorkflow(name, node, start, context, WAITING);
    }

    public Workflow createOnHoldNow(String name, String node, Value context) {
        return createWorkflow(name, node, currentTimeMillis(), context, INITIALISING);
    }

    public Workflow createOnHoldScheduled(String name, String node, Value context, long start) {
        return createWorkflow(name, node, start, context, INITIALISING);
    }

    public void saveChanges(Workflow workflow) {
        workflow.setUpdated(currentTimeMillis());
        workflowCollection.save(workflow);
    }

    public void activateOnHoldDependencies(Workflow workflow) {
        for (ObjectId dependency : workflow.getDependencies()) {
            workflowCollection.update("{_id:#, status:#}", dependency, INITIALISING)
                    .with("{$set:{status:#, updated:#}}", WAITING, currentTimeMillis());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Workflow workflow = getWaiting();
                if (workflow != null) {
                    process(workflow);
                } else {
                    sleep(workflowServiceSleep);
                }
            }
        } catch (InterruptedException ie) {
            // ignored
        }
    }

    Workflow getWaiting() {
        long millis = currentTimeMillis();
        return workflowCollection.findAndModify("{status:#, start:{$lte:#}, dependencies:{$size:0}}", WAITING, millis)
                .with("{status:#, updated:#}", PROCESSING, millis)
                .returnNew()
                .as(Workflow.class);
    }

    void process(Workflow workflow) {
        try {
            WorkflowNode node = getNode(workflow.getNode());
            if (node != null && node.getName() != null) {

                WorkflowCommand command = workflowCommandFactory.getCommand(node.getName());
                if (command != null) {

                    command.execute(workflow);

                    if (node.getName().equals(workflow.getNode())) {
                        workflow.setNode(node.getNext());
                    }

                    if (workflow.getNode() == null) {
                        completed(workflow);
                    } else {
                        activate(workflow, node);
                    }

                } else {
                    error(workflow, ERR_COMMAND, WORKFLOW_COMMAND_FACTORY_RETURNED_NULL);
                }
            } else {
                error(workflow, ERR_NODE, WORKFLOW_NODE_MISSING);
            }
        } catch (WorkflowException e) {
            error(workflow, e.getClassification(), e.getMessage());
        }
    }

    private WorkflowNode getNode(String name) {
        return workflowNodeCollection.findOne("{_id:#}", name).as(WorkflowNode.class);
    }

    private void completed(Workflow workflow) {
        workflowCollection.update("{dependencies:#}", workflow.getId())
                .multi()
                .with("{'$pull': {dependencies:#}}, '$set':{updated:#}}", workflow.getId(), currentTimeMillis());
        workflowCollection.remove(workflow.getId());
    }

    private void activate(Workflow workflow, WorkflowNode node) {
        workflowCollection.update(
                "{_id:#, status:#, node:#, retry:#}", workflow.getId(), PROCESSING, node.getName(), workflow.getRetry())
                .with("{'$set':{node:#, status:#, start:#, updated:#, retry:0, context:#, dependencies:#}}",
                        workflow.getNode(),
                        WAITING,
                        workflow.getStart(),
                        currentTimeMillis(),
                        workflow.getContext(),
                        workflow.getDependencies()
                );
    }

    private void error(Workflow workflow, String errorCode, String message) {
        WorkflowError workflowError = new WorkflowError(workflow, errorCode, message);
        workflowLogCollection.save(workflowError);
        workflowCollection.remove(workflow.getId());
    }
}
