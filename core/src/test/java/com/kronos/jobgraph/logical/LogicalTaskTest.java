package com.kronos.jobgraph.logical;

import com.kronos.jobgraph.JobConfiguration;
import com.kronos.mock.MockConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: jackila
 * @Date: 15:16 2022-12-14
 */
public class LogicalTaskTest {

    private JobConfiguration mockConfig;

    @Before
    public void init() {
        mockConfig = MockConfiguration.mockJobConfiguration();
    }

    @Test
    public void testConvertToHandlerTree() {
        LogicalGraph logicalTask = new LogicalGraph();
        TransformerLogicalNode root = logicalTask.convertToHandlerTree(mockConfig);
        Assert.assertEquals(mockConfig.getMainTable(), root.target);
        List<String> path = traverPath(root);
        Assert.assertEquals(2, path.size());
        Assert.assertTrue(path.contains("student,grade"));
        Assert.assertTrue(path.contains("student,optional_course,teacher_info"));
    }

    private List<String> traverPath(TransformerLogicalNode root) {
        List<List<String>> ret = new ArrayList<>();
        traverPath(root, new ArrayList(), ret);
        return ret.stream().map(t -> StringUtils.join(t, ",")).collect(Collectors.toList());
    }

    private void traverPath(TransformerLogicalNode root,
                            List<String> path,
                            List<List<String>> ret) {
        if (root == null) {
            ret.add(new ArrayList<>(path));
            return;
        }

        path.add(root.target.getObjectName());
        List<TransformerLogicalNode> child = root.getChild();
        if (child == null || child.size() == 0) {
            ret.add(new ArrayList<>(path));
        } else {
            for (int i = 0; i < child.size(); i++) {
                traverPath(child.get(i), path, ret);
            }
        }
        path.remove(path.size() - 1);
    }
}