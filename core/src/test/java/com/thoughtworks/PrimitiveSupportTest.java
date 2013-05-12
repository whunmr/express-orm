package com.thoughtworks;

import com.thoughtworks.fixture.DayEnum;
import com.thoughtworks.fixture.Misc;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PrimitiveSupportTest extends BaseDBTest{
    @Test
    public void should_able_to_mapping_primitive_members_to_db_columns() {
        truncateTable("miscs");
        Misc misc = new Misc();
        misc.boolValue = true;
        misc.charValue = 'c';
        misc.byteValue = 4;
        misc.shortValue = 8;
        misc.integerValue = 15;
        misc.longValue = 16L;
        misc.floatValue = 23.0f;
        misc.doubleValue = 42.0d;
        misc.stringValue = "LOST";

        misc.primitiveIntValue = 4;
        misc.primitiveDoubleValue = 8;
        misc.primitiveFloatValue = 15.0f;
        misc.primitiveBooleanValue = true;
        misc.primitiveShortValue = 23;
        misc.primitiveLongValue = 42L;
        misc.primitiveByteValue = 0;

        misc.day = DayEnum.MONDAY;

        misc.save();

        Misc miscInDB = Misc.find_by_id(1);
        assertThat(miscInDB, equalTo(misc));
    }
}
