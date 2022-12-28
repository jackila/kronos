package com.kronos.config;

import com.kronos.jobgraph.JobConfiguration;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Author: jackila
 * @Date: 3:48 PM 2022-5-30
 */
public class JobConfigurationTest {

    @Test
    public void testLoad() {
        JobConfiguration config = JobConfiguration.load("example.yml");
        Assert.assertEquals(config.findRelationTable("student.id").getTarget(), config.getMainTable());
    }
}