/*
 * EdgeServiceTest.java
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

package thingynet.graph;

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
import java.util.Set;

import static java.lang.System.currentTimeMillis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class EdgeServiceTest {
    private static final StringValue STRING_VALUE = new StringValue("String Value");
    private static final Set<String> TRAITS = new HashSet<>();

    static {
        TRAITS.add("Trait");
    }

    private static final String TYPE = "TYPE";
    private static final String FROM = "FROM";
    private static final Vertex VERTEX_FROM = new Vertex(FROM, TYPE, TRAITS, STRING_VALUE);
    private static final String TO = "TO";
    private static final Vertex VERTEX_TO = new Vertex(TO, TYPE, TRAITS, STRING_VALUE);
    private static final String LABEL = "Label";
    private static final String SEPARATOR = "§";
    private static final String EID =
            VERTEX_FROM.getType() + SEPARATOR
                    + LABEL + SEPARATOR
                    + VERTEX_TO.getType() + SEPARATOR
                    + VERTEX_FROM.getName() + SEPARATOR
                    + VERTEX_TO.getName();
    private static final long SINCE = 1l;
    private static final long UNTIL = 2l;
    private static final double WEIGHT = 1.0;
    @Autowired
    private EdgeService edgeService;

    @Autowired
    private MongoCollection edgeCollection;
    private long start;

    @Before
    public void before() {
        edgeCollection.remove();
        start = currentTimeMillis();
    }

    @Test
    public void createEdgeShouldReturnEdgeWithExpectedValues() {
        Edge edge = edgeService.createEdge(VERTEX_FROM, LABEL, VERTEX_TO, TRAITS, SINCE, UNTIL, WEIGHT, STRING_VALUE);

        assertThat(edge, notNullValue());
        assertThat(edge.getEid(), is(EID));
        assertThat(edge.getFrom(), is(VERTEX_FROM.getVid()));
        assertThat(edge.getLabel(), is(LABEL));
        assertThat(edge.getTo(), is(VERTEX_TO.getVid()));
        assertThat(edge.getTraits(), is(TRAITS));
        assertThat(edge.getSince(), is(SINCE));
        assertThat(edge.getUntil(), is(UNTIL));
        assertThat(edge.getWeight(), is(WEIGHT));
        assertThat(edge.getPayload().getValue(), is(STRING_VALUE.getValue()));
        assertThat(edge.getUpdated(), greaterThanOrEqualTo(start));
        assertThat(edge.getPayload().getValue(), is(STRING_VALUE.getValue()));
    }

    @Test
    public void createEdgeShouldSaveEdgeWithExpectedValues() {
        assertThat(edgeCollection.count(), is(0l));

        Edge edge = edgeService.createEdge(VERTEX_FROM, LABEL, VERTEX_TO, TRAITS, SINCE, UNTIL, WEIGHT, STRING_VALUE);

        Edge actual = edgeCollection.findOne("{_id:#}", edge.getEid()).as(Edge.class);
        assertThat(actual.getEid(), is(EID));
        assertThat(edge.getFrom(), is(VERTEX_FROM.getVid()));
        assertThat(edge.getLabel(), is(LABEL));
        assertThat(edge.getTo(), is(VERTEX_TO.getVid()));
        assertThat(edge.getTraits(), is(TRAITS));
        assertThat(edge.getSince(), is(SINCE));
        assertThat(edge.getUntil(), is(UNTIL));
        assertThat(edge.getWeight(), is(WEIGHT));
        assertThat(actual.getPayload().getValue(), is(STRING_VALUE.getValue()));
        assertThat(actual.getUpdated(), greaterThanOrEqualTo(start));
        assertThat(actual.getPayload().getValue(), is(STRING_VALUE.getValue()));
    }

    @Test(expected = NullPointerException.class)
    public void createEdgeWithNullFromShouldThrowNPE() {
        edgeService.createEdge(null, LABEL, VERTEX_TO, TRAITS, SINCE, UNTIL, WEIGHT, STRING_VALUE);
    }

    @Test(expected = NullPointerException.class)
    public void createEdgeWithNullLabelShouldThrowNPE() {
        edgeService.createEdge(VERTEX_FROM, null, VERTEX_TO, TRAITS, SINCE, UNTIL, WEIGHT, STRING_VALUE);
    }

    @Test(expected = NullPointerException.class)
    public void createEdgeWithNullToShouldThrowNPE() {
        edgeService.createEdge(VERTEX_FROM, LABEL, null, TRAITS, SINCE, UNTIL, WEIGHT, STRING_VALUE);
    }

}