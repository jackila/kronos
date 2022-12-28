package com.kronos.jobgraph.physic;

/**
 * @Author: jackila
 * @Date: 10:04 AM 2022-7-23
 */
public class StreamRecord<T> {
    private volatile T value;
    public StreamRecord(T value) {
        this.value = value;
    }
    public <X> StreamRecord<X> replace(X value ) {
        this.value = (T) value;

        return (StreamRecord<X>) this;
    }

    public T value() {
        return value;
    }

    public void  setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("StreamRecord{");
        sb.append("value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
