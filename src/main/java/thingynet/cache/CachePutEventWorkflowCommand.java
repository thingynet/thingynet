/*
 * CachePutEventWorkflowCommand.java
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

package thingynet.cache;

import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import thingynet.workflow.Workflow;
import thingynet.workflow.WorkflowCommand;
import thingynet.workflow.WorkflowException;

@Component
public class CachePutEventWorkflowCommand implements WorkflowCommand {

    public static final String CACHE_KEY = "cache.key";
    @Autowired
    MongoCollection cacheEventCollection;

    @Override
    public void execute(Workflow workflow) throws WorkflowException {
// TODO -fix        cacheEventCollection.save(new Event(CACHE_PUT_EVENT, workflow.getContext().get(CACHE_KEY)));
    }

    @Override
    public long getTimeout() {
        return 0;
    }

}
