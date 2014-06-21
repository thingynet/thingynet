/*
 * MapValueTest.java
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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class MapValueTest {
    private static final String KEY_1 = "Key 1";
    private static final String KEY_2 = "Key 2";
    private static final StringValue STRING_VALUE = new StringValue("String Value");
    private static final IntegerValue INTEGER_VALUE = new IntegerValue(1);
    private static final BooleanValue BOOLEAN_VALUE = new BooleanValue(true);
    private static final LongValue LONG_VALUE = new LongValue(1l);

    private MapValue mapValue;

    @Autowired
    private MongoCollection valueCollection;

    @Before
    public void before() {
        valueCollection.remove();

        mapValue = new MapValue();

        HashMap<String, Value> value = new HashMap<>();
        mapValue.setValue(value);
        value.put(KEY_1, STRING_VALUE);
        value.put(KEY_2, INTEGER_VALUE);
    }

    @Test
    public void saveShouldSaveExpectedValues() {
        ValueDocument doc = new ValueDocument();
        doc.setValue(mapValue);

        valueCollection.save(doc);

        ValueDocument actual = valueCollection.findOne(doc.getId()).as(ValueDocument.class);
        MapValue value = (MapValue) actual.getValue();

        StringValue key1Value = (StringValue) value.getValue().get(KEY_1);
        assertThat(key1Value.getValue(), is(STRING_VALUE.getValue()));

        IntegerValue key2Value = (IntegerValue) value.getValue().get(KEY_2);
        assertThat(key2Value.getValue(), is(INTEGER_VALUE.getValue()));
    }

    @Test
    public void updateShouldSaveExpectedValues() {
        ValueDocument doc = new ValueDocument();
        doc.setValue(mapValue);

        valueCollection.save(doc);

        Map map = mapValue.getValue();
        map.put(KEY_1, BOOLEAN_VALUE);
        map.put(KEY_2, LONG_VALUE);
        valueCollection.update("{_id:#}", doc.getId()).with("{'$set':{value:#}}", mapValue);

        ValueDocument actual = valueCollection.findOne(doc.getId()).as(ValueDocument.class);
        MapValue value = (MapValue) actual.getValue();

        BooleanValue key1Value = (BooleanValue) value.getValue().get(KEY_1);
        assertThat(key1Value.getValue(), is(BOOLEAN_VALUE.getValue()));

        LongValue key2Value = (LongValue) value.getValue().get(KEY_2);
        assertThat(key2Value.getValue(), is(LONG_VALUE.getValue()));
    }

}
