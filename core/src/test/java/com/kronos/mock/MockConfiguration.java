package com.kronos.mock;

import com.kronos.jobgraph.JobConfiguration;

/**
 * example的例子可以解析成如下的拓扑图 x x x x ┌───────┐ ┌────────────────┐ x x ┌───────────────┐ ┌────────┐
 * │teacher├──────► optional course│ x x │optional course├─────────►teacher │ └───────┘
 * └────────┬───────┘ x x └──────▲────────┘ └────────┘ │ x x │ │ x ┌───────┐ x │
 * ├──────────────►student├────────────┤ │ x └───────┘ x │ ┌────┴───┐ x x ┌────▼────┐ │ grade │ x x
 * │ grade │ └────────┘ x x └─────────┘ x x
 * ────────────────────────────────────────────────────────────────────────────────────────── front
 * stage x middle stage x back stage x x x x
 *
 * <p>the model can be find in example.yml 2022-12-14
 */
public class MockConfiguration {

    private static final String mockExample = "example.yml";

    public static JobConfiguration mockJobConfiguration() {
        JobConfiguration mock = JobConfiguration.load(mockExample);
        return mock;
    }
}
