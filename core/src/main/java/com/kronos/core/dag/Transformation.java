package com.kronos.core.dag;

/**
 * @Author: jackila
 * @Date: 11:47 AM 2022-6-18
 */
public abstract class Transformation<T> {

    private String name;

    public Transformation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
