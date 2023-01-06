package com.kronos.config;

import java.util.Map;

/** 2022-10-26 */
public class Configuration {
    private Map container;

    public long getLong(ConfigOption<Long> sourceReaderCloseTimeout) {

        Object value =
                container.getOrDefault(
                        sourceReaderCloseTimeout.key(), sourceReaderCloseTimeout.defaultValue());
        return Long.valueOf(value.toString());
    }

    public int getInteger(ConfigOption<Integer> elementQueueCapacity) {
        Object value =
                container.getOrDefault(
                        elementQueueCapacity.key(), elementQueueCapacity.defaultValue());
        return Integer.valueOf(value.toString());
    }
}
