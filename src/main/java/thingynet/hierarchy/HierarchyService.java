/*
 * HierarchyService.java
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

package thingynet.hierarchy;

import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import thingynet.value.Value;

@Service
public class HierarchyService {

    @Autowired
    private MongoCollection hierarchyCollection;

    public Hierarchy createRoot(String name, Value payload) {
        Hierarchy root = new Hierarchy(name, name, 0, payload);
        hierarchyCollection.save(root);
        return root;
    }

    public Hierarchy createChild(Hierarchy hierarchy, String name, Value payload) {
        Hierarchy child = new Hierarchy();
        child.setName(name);
        child.setPath(hierarchy.getPath() + "§" + name);
        child.setDepth(hierarchy.getDepth() + 1);
        child.setPayload(payload);
        hierarchyCollection.save(child);
        return child;
    }

    public Hierarchy getHierarchy(String path) {
        return hierarchyCollection.findOne("{_id:#}", path).as(Hierarchy.class);
    }

    public Hierarchy getParent(Hierarchy hierarchy) {
        if (hierarchy.getDepth() == 0) {
            return null;
        }
        return hierarchyCollection.findOne(
                "{_id:#}", hierarchy.getPath().substring(0, hierarchy.getPath().lastIndexOf('§')))
                .as(Hierarchy.class);
    }

    public Iterable<Hierarchy> getChildren(Hierarchy hierarchy) {
        return getDescendants(hierarchy, hierarchy.getDepth() + 1);
    }

    public Iterable<Hierarchy> getDescendants(Hierarchy hierarchy) {
        return hierarchyCollection.find(
                "{_id:{$regex:#}}", "^" + hierarchy.getPath() + "§")
                .as(Hierarchy.class);
    }

    public Iterable<Hierarchy> getDescendants(Hierarchy hierarchy, int levels) {
        return hierarchyCollection.find(
                "{_id:{$regex:#}, depth:{$lte:#}}",
                "^" + hierarchy.getPath() + "§",
                hierarchy.getDepth() + levels)
                .as(Hierarchy.class);
    }

    // TODO Add save + remove with descendants & updated field
}
