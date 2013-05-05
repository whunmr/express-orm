package com.thoughtworks.fixture;

import com.thoughtworks.Model;

public class Misc extends Model<Misc> {
    public Boolean boolValue;
    public Character charValue;
    public Byte byteValue;
    public Short shortValue;
    public Integer integerValue;
    public Long longValue;
    public Float floatValue;
    public Double doubleValue;
    public String stringValue;

    public int primitiveIntValue;
    public double primitiveDoubleValue;
    public float primitiveFloatValue;
    public boolean primitiveBooleanValue;
    public short primitiveShortValue;
    public long primitiveLongValue;
    public byte primitiveByteValue;

    public DayEnum day;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Misc misc = (Misc) o;

        if (primitiveBooleanValue != misc.primitiveBooleanValue) return false;
        if (primitiveByteValue != misc.primitiveByteValue) return false;
        if (Double.compare(misc.primitiveDoubleValue, primitiveDoubleValue) != 0) return false;
        if (Float.compare(misc.primitiveFloatValue, primitiveFloatValue) != 0) return false;
        if (primitiveIntValue != misc.primitiveIntValue) return false;
        if (primitiveLongValue != misc.primitiveLongValue) return false;
        if (primitiveShortValue != misc.primitiveShortValue) return false;
        if (boolValue != null ? !boolValue.equals(misc.boolValue) : misc.boolValue != null) return false;
        if (byteValue != null ? !byteValue.equals(misc.byteValue) : misc.byteValue != null) return false;
        if (charValue != null ? !charValue.equals(misc.charValue) : misc.charValue != null) return false;
        if (doubleValue != null ? !doubleValue.equals(misc.doubleValue) : misc.doubleValue != null) return false;
        if (floatValue != null ? !floatValue.equals(misc.floatValue) : misc.floatValue != null) return false;
        if (integerValue != null ? !integerValue.equals(misc.integerValue) : misc.integerValue != null) return false;
        if (longValue != null ? !longValue.equals(misc.longValue) : misc.longValue != null) return false;
        if (shortValue != null ? !shortValue.equals(misc.shortValue) : misc.shortValue != null) return false;
        if (stringValue != null ? !stringValue.equals(misc.stringValue) : misc.stringValue != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = boolValue != null ? boolValue.hashCode() : 0;
        result = 31 * result + (charValue != null ? charValue.hashCode() : 0);
        result = 31 * result + (byteValue != null ? byteValue.hashCode() : 0);
        result = 31 * result + (shortValue != null ? shortValue.hashCode() : 0);
        result = 31 * result + (integerValue != null ? integerValue.hashCode() : 0);
        result = 31 * result + (longValue != null ? longValue.hashCode() : 0);
        result = 31 * result + (floatValue != null ? floatValue.hashCode() : 0);
        result = 31 * result + (doubleValue != null ? doubleValue.hashCode() : 0);
        result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
        result = 31 * result + primitiveIntValue;
        temp = primitiveDoubleValue != +0.0d ? Double.doubleToLongBits(primitiveDoubleValue) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (primitiveFloatValue != +0.0f ? Float.floatToIntBits(primitiveFloatValue) : 0);
        result = 31 * result + (primitiveBooleanValue ? 1 : 0);
        result = 31 * result + (int) primitiveShortValue;
        result = 31 * result + (int) (primitiveLongValue ^ (primitiveLongValue >>> 32));
        result = 31 * result + (int) primitiveByteValue;
        return result;
    }
}
