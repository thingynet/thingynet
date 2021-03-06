/*
 * StringValueTest.java
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

package thingynet.value;

import org.jongo.MongoCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import thingynet.Application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class StringValueTest {
    private static final StringValue SAVE_VALUE = new StringValue("Save Value");
    private static final StringValue UPDATE_VALUE = new StringValue("Update Value");

    @Autowired
    private MongoCollection valueCollection;

    @Before
    public void before() {
        valueCollection.remove();
    }

    @Test
    public void saveShouldSaveExpectedValues() {
        ValueDocument doc = new ValueDocument();
        doc.setValue(SAVE_VALUE);

        valueCollection.save(doc);

        ValueDocument actual = valueCollection.findOne(doc.getId()).as(ValueDocument.class);

        StringValue value = (StringValue) actual.getValue();
        assertThat(value.getValue(), is(SAVE_VALUE.getValue()));
    }

    @Test
    public void updateShouldSaveExpectedValues() {
        ValueDocument doc = new ValueDocument();
        doc.setValue(SAVE_VALUE);

        valueCollection.save(doc);

        valueCollection.update("{_id:#}", doc.getId()).with("{'$set':{value:#}}", UPDATE_VALUE);

        ValueDocument actual = valueCollection.findOne(doc.getId()).as(ValueDocument.class);

        StringValue value = (StringValue) actual.getValue();
        assertThat(value.getValue(), is(UPDATE_VALUE.getValue()));
    }

}
