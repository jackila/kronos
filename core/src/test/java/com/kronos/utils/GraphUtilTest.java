package com.kronos.utils;

import com.kronos.jobgraph.logical.TransformerLogicalNode;
import com.kronos.jobgraph.physic.TPhysicalNode;
import com.kronos.jobgraph.table.ObjectPath;
import org.apache.commons.compress.utils.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * @Author: jackila
 * @Date: 19:57 2022-12-16
 */
class GraphUtilTest {
    private TransformerLogicalNode root;
    private String[] expect;

    /*
     *                               +---+
     *               +---------------+ A +---------------+
     *               |               +---+               |
     *               |                                   |
     *               |                                   |
     *               |                                   |
     *               |                                   |
     *             +-v-+                               +-v-+
     *             | B |                               | C |
     *   +---------+-+-+----------+                    +---+
     *   |           |            |
     *   |           |            |
     *   |           |            |
     *   |           |            |
     * +-v-+       +-v-+        +-v-+
     * | D |       | E |        | F |
     * +-+-+       +---+        +---+
     *   |
     *   |
     *   |
     *   |
     *   |
     * +-v-+
     * | G |
     * +---+
     */
    @BeforeEach
    public void before() {
        TransformerLogicalNode A = new TransformerLogicalNode(new ObjectPath("A"));
        TransformerLogicalNode B = new TransformerLogicalNode(new ObjectPath("B"));
        TransformerLogicalNode C = new TransformerLogicalNode(new ObjectPath("C"));
        TransformerLogicalNode D = new TransformerLogicalNode(new ObjectPath("D"));
        TransformerLogicalNode E = new TransformerLogicalNode(new ObjectPath("E"));
        TransformerLogicalNode F = new TransformerLogicalNode(new ObjectPath("F"));
        TransformerLogicalNode G = new TransformerLogicalNode(new ObjectPath("G"));

        A.addChild(B).addChild(C);
        B.addChild(D).addChild(E).addChild(F);
        D.addChild(G);

        root = A;
        expect = new String[]{"A", "B", "C", "D", "E", "F", "G"};
    }
    @Test
    void convertToPhysicalGraph() {
    }
    @Test
    void convertToPhysicalNode() {

        TPhysicalNode tPhysicalNode = GraphUtil.convertToPhysicalNode(root, null);
        String[] acture = traverTree(tPhysicalNode).toArray(new String[0]);
        Arrays.sort(acture);

        Assert.assertArrayEquals(expect, acture);
    }

    private List<String> traverTree(TPhysicalNode root) {
        List<String> ret = Lists.newArrayList();

        Deque<TPhysicalNode> stack = new ArrayDeque<>();
        stack.add(root);
        while (!stack.isEmpty()) {
            TPhysicalNode head = stack.poll();
            ret.add(head.getTarget().getObjectName());
            for (int i = 0; head.getNodes() != null && i < head.getNodes().size(); i++) {
                stack.push(head.getNodes().get(i));
            }
        }
        return ret;
    }

}