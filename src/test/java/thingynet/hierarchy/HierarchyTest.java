/*
 * HierarchyTest.java
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import thingynet.Application;
import thingynet.value.StringValue;

import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class HierarchyTest {

    private static final StringValue STRING_VALUE = new StringValue("String Value");

    private static final String PARENT = "PARENT";
    private static final String CHILD = "CHILD";
    private static final String SIBLING = "SIBLING";
    private static final String GRAND_CHILD = "GRAND_CHILD";
    private static final String GREAT_GRAND_CHILD = "GREAT_GRAND_CHILD";
    private static final String OTHER = "OTHER";

    @Autowired
    private MongoCollection hierarchyCollection;

    @Autowired
    private HierarchyService hierarchyService;


    @Before
    public void before() {
        hierarchyCollection.remove();
    }

    @Test
    public void createRootShouldReturnHierarchyWithExpectedValue() {
        Hierarchy parent = hierarchyService.createRoot(PARENT, STRING_VALUE);

        assertThat(parent, notNullValue());
        assertThat(parent.getName(), is(PARENT));
        assertThat(parent.getPath(), is(PARENT));
        assertThat(parent.getDepth(), is(0));
        assertThat(parent.getPayload().getValue(), is(STRING_VALUE.getValue()));
    }

    @Test
    public void createRootShouldSaveHierarchyWithExpectedValue() {
        assertThat(hierarchyCollection.count(), is(0l));

        Hierarchy parent = hierarchyService.createRoot(PARENT, STRING_VALUE);

        Hierarchy actual = hierarchyCollection.findOne("{_id:#}", PARENT).as(Hierarchy.class);
        assertThat(actual, notNullValue());
        assertThat(parent.getName(), is(PARENT));
        assertThat(actual.getPath(), is(PARENT));
        assertThat(actual.getDepth(), is(0));
        assertThat(actual.getPayload().getValue(), is(STRING_VALUE.getValue()));
    }

    @Test
    public void createChildShouldReturnHierarchyWithExpectedValue() {
        Hierarchy parent = hierarchyService.createRoot(PARENT, STRING_VALUE);
        Hierarchy child = hierarchyService.createChild(parent, CHILD, STRING_VALUE);

        assertThat(child, notNullValue());
        assertThat(child.getName(), is(CHILD));
        assertThat(child.getPath(), is(PARENT + "§" + CHILD));
        assertThat(child.getDepth(), is(1));
        assertThat(child.getPayload().getValue(), is(STRING_VALUE.getValue()));
    }

    @Test
    public void createChildShouldSaveHierarchyWithExpectedValue() {
        Hierarchy parent = hierarchyService.createRoot(PARENT, STRING_VALUE);
        Hierarchy child = hierarchyService.createChild(parent, CHILD, STRING_VALUE);

        Hierarchy actual = hierarchyCollection.findOne("{_id:#}", child.getPath()).as(Hierarchy.class);

        assertThat(actual, notNullValue());
        assertThat(actual.getName(), is(CHILD));
        assertThat(actual.getPath(), is(PARENT + "§" + CHILD));
        assertThat(actual.getDepth(), is(1));
        assertThat(actual.getPayload().getValue(), is(STRING_VALUE.getValue()));
    }

    @Test
    public void getHierarchyShouldReturnNullWhenNoneExists() {
        assertThat(hierarchyCollection.count(), is(0l));

        Hierarchy actual = hierarchyService.getHierarchy(PARENT);
        assertThat(actual, nullValue());
    }

    @Test
    public void getHierarchyShouldReturnNullWhenNoMatchExists() {
        hierarchyService.createRoot(PARENT, null);
        assertThat(hierarchyCollection.count(), is(1l));

        Hierarchy actual = hierarchyService.getHierarchy(CHILD);
        assertThat(actual, nullValue());
    }

    @Test
    public void getHierarchyShouldReturnHierarchyWhenExists() {
        hierarchyService.createRoot(PARENT, STRING_VALUE);
        assertThat(hierarchyCollection.count(), is(1l));

        Hierarchy actual = hierarchyService.getHierarchy(PARENT);
        assertThat(actual, notNullValue());
        assertThat(actual.getName(), is(PARENT));
        assertThat(actual.getPath(), is(PARENT));
        assertThat(actual.getDepth(), is(0));
        assertThat(actual.getPayload().getValue(), is(STRING_VALUE.getValue()));
    }

    @Test
    public void getParentShouldReturnNullWhenNoParentExists() {
        Hierarchy parent = hierarchyService.createRoot(PARENT, STRING_VALUE);

        Hierarchy actual = hierarchyService.getParent(parent);
        assertThat(actual, nullValue());
    }

    @Test
    public void getParentShouldReturnExpectedHierarchyWhenParentExists() {
        Hierarchy parent = hierarchyService.createRoot(PARENT, STRING_VALUE);
        Hierarchy child = hierarchyService.createChild(parent, CHILD, null);
        Hierarchy grandChild = hierarchyService.createChild(child, GRAND_CHILD, null);
        hierarchyService.createChild(grandChild, GREAT_GRAND_CHILD, null);

        Hierarchy actual = hierarchyService.getParent(grandChild);
        assertThat(actual.getPath(), is(child.getPath()));
    }

    @Test
    public void getChildrenShouldReturnExpectedHierarchies() {
        Hierarchy parent = hierarchyService.createRoot(PARENT, null);
        Hierarchy child1 = hierarchyService.createChild(parent, CHILD, null);
        Hierarchy child2 = hierarchyService.createChild(parent, SIBLING, null);
        hierarchyService.createRoot(OTHER, null);

        Iterable<Hierarchy> actual = hierarchyService.getChildren(parent);
        assertThat(actual.iterator().next().getPath(), is(child1.getPath()));
        assertThat(actual.iterator().next().getPath(), is(child2.getPath()));
        assertThat(actual.iterator().hasNext(), is(false));
    }

    @Test
    public void getDescendantsShouldReturnExpectedHierarchies() {
        Hierarchy parent = hierarchyService.createRoot(PARENT, null);
        Hierarchy child = hierarchyService.createChild(parent, CHILD, null);
        Hierarchy sibling = hierarchyService.createChild(parent, SIBLING, null);
        Hierarchy grandChild = hierarchyService.createChild(child, GRAND_CHILD, null);
        Hierarchy greatGrandChild = hierarchyService.createChild(grandChild, GREAT_GRAND_CHILD, null);

        Hierarchy other = hierarchyService.createRoot(OTHER, null);
        hierarchyService.createChild(other, CHILD, null);

        HashSet<String> expectedIds = new HashSet<>();
        expectedIds.add(child.getPath());
        expectedIds.add(sibling.getPath());
        expectedIds.add(grandChild.getPath());
        expectedIds.add(greatGrandChild.getPath());

        Iterable<Hierarchy> actual = hierarchyService.getDescendants(parent);
        while (actual.iterator().hasNext()) {
            Hierarchy descendant = actual.iterator().next();
            assertThat(expectedIds.remove(descendant.getPath()), is(true));
        }
        assertThat(expectedIds.size(), is(0));
    }

    @Test
    public void getDescendantsWithLevelShouldReturnExpectedHierarchies() {
        Hierarchy parent = hierarchyService.createRoot(PARENT, null);
        Hierarchy child = hierarchyService.createChild(parent, CHILD, null);
        hierarchyService.createChild(parent, SIBLING, null);
        Hierarchy grandChild = hierarchyService.createChild(child, GRAND_CHILD, null);
        Hierarchy greatGrandChild = hierarchyService.createChild(grandChild, GREAT_GRAND_CHILD, null);
        hierarchyService.createChild(greatGrandChild, CHILD, null);

        HashSet<String> expectedIds = new HashSet<>();
        expectedIds.add(grandChild.getPath());
        expectedIds.add(greatGrandChild.getPath());

        Iterable<Hierarchy> actual = hierarchyService.getDescendants(child, 2);
        while (actual.iterator().hasNext()) {
            Hierarchy descendant = actual.iterator().next();
            assertThat(expectedIds.remove(descendant.getPath()), is(true));
        }
        assertThat(expectedIds.size(), is(0));
    }

}
