package com.kronos.cdc.data.source;

/**
 * @Author: jackila
 * @Date: 19:22 2022/12/17
 */
public class RawDataType {

    private final String typeName;
    private final int typeId;

    RawDataType(String typeName,
                int typeId) {
        this.typeName = typeName;
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getTypeId() {
        return typeId;
    }

    public static RawDataType of(int dataTypeId) {
        return of("UNKNOWN", dataTypeId);
    }

    public static RawDataType of(String dataTypeName,
                                 int dataTypeId) {
        return new RawDataType(dataTypeName, dataTypeId);
    }



    public static RawDataType copy(RawDataType source) {
        return RawDataType.of(source.getTypeName(), source.getTypeId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RawDataType that = (RawDataType) o;

        if (typeId != that.typeId) return false;
        return typeName.equals(that.typeName);
    }

    @Override
    public int hashCode() {
        int result = typeName.hashCode();
        result = 31 * result + typeId;
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RawDataType{");
        sb.append("typeName='").append(typeName).append('\'');
        sb.append(", typeId=").append(typeId);
        sb.append('}');
        return sb.toString();
    }
}
