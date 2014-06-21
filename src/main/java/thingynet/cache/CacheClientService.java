/*
 * CacheClientService.java
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
import org.springframework.stereotype.Service;
import thingynet.value.Value;
import thingynet.workflow.WorkflowService;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class CacheClientService {
    private static final String CACHE_KEY_QUERY = "{cacheKey:#}";
    private final HashMap<String, CountDownLatch> latches = new HashMap<>();
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    @Autowired
    WorkflowService workflowService;
    @Autowired
    MongoCollection cacheCollection;

    public Value get(CacheClient client) throws InterruptedException {
        Value value = (Value) cacheCollection.findOne(CACHE_KEY_QUERY, client.getCacheKey());
        if (value == null) {
            waitForCachePut(client);
            value = (Value) cacheCollection.findOne(CACHE_KEY_QUERY, client.getCacheKey());
        }
        return value;
    }

    void waitForCachePut(CacheClient client) throws InterruptedException {
        rwl.writeLock().lock();
        CountDownLatch latch = latches.get(client.getCacheKey());
        if (latch == null) {
            latch = new CountDownLatch(1);
            latches.put(client.getCacheKey(), latch);
// TODO fix            workflowService.createReadyNow("CachePut", client.getCachePutNode().getName(), client.getCachePutContext());
        }
        rwl.writeLock().unlock();
        latch.await();
    }

    void notifyCachePut(String key) {
        if (key != null) {
            rwl.writeLock().lock();
            CountDownLatch latch = latches.remove(key);
            if (latch != null) {
                latch.countDown();
            }
            rwl.writeLock().unlock();
        }
    }
}
