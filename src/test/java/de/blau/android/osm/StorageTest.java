package de.blau.android.osm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import android.util.Log;
import de.blau.android.exception.OsmException;

public class StorageTest {

    private static final String DEBUG_TAG = "StorageTest";
    private Storage             storage;

    /**
     * Pre-test setup
     */
    @Before
    public void setup() {
        storage = PbfTest.read();
        Log.d(DEBUG_TAG, "Loaded " + storage.getNodeCount() + " Nodes " + storage.getWayCount() + " Ways " + storage.getRelationCount() + " Relations");
    }

    /**
     * Ways for node
     */
    @Test
    public void waysForNode() {
        Node node = (Node) storage.getOsmElement(Node.NAME, 300852915L);
        assertNotNull(node);
        long start = System.currentTimeMillis();
        List<Way> ways = storage.getWays(node);
        long execution = System.currentTimeMillis() - start;
        Log.d(DEBUG_TAG, "getWays(Node) took " + execution + " ms");
        assertEquals(2, ways.size());
    }

    /**
     * Check if a Node is an end node of any way in storage
     */
    @Test
    public void isEndNode() {
        assertTrue(storage.isEndNode(storage.getNode(300852915L)));
    }

    /**
     * Nodes for BoundingBox
     */
    @Test
    public void nodesForBoundingBox() {
        BoundingBox box = new BoundingBox(9.51947D, 47.13638D, 9.52300D, 47.14066D);
        long start = System.currentTimeMillis();
        List<Node> nodes = storage.getNodes(box);
        long execution = System.currentTimeMillis() - start;
        Log.d(DEBUG_TAG, "getNodes(Boundingbox) took " + execution + " ms");
        assertEquals(1260, nodes.size());
    }

    /**
     * Ways for BoundingBox
     */
    @Test
    public void waysForBoundingBox() {
        BoundingBox box = new BoundingBox(9.51947D, 47.13638D, 9.52300D, 47.14066D);
        long start = System.currentTimeMillis();
        List<Way> ways = storage.getWays(box);
        long execution = System.currentTimeMillis() - start;
        Log.d(DEBUG_TAG, "getWays(Boundingbox) took " + execution + " ms");
        assertEquals(217, ways.size());
    }

    /**
     * Calculate a bounding box from the loaded data
     */
    @Test
    public void calcBoundingBoxFromData() {
        try {
            BoundingBox box = storage.calcBoundingBoxFromData();
            assertEquals(63299846, box.getLeft());
            assertEquals(460876616, box.getBottom());
            assertEquals(99643825, box.getRight());
            assertEquals(475258072, box.getTop());
        } catch (OsmException e) {
            fail();
        }
    }

    /**
     * Get the last bounding box
     */
    @Test
    public void boundingBoxes() {
        BoundingBox box1 = storage.getLastBox();
        assertEquals(94710780, box1.getLeft());
        assertEquals(470477400, box1.getBottom());
        assertEquals(96362170, box1.getRight());
        assertEquals(472712800, box1.getTop());
        assertEquals(1, storage.getBoundingBoxes().size());
        storage.clearBoundingBoxList();
        assertEquals(0, storage.getBoundingBoxes().size());
        BoundingBox box = storage.getLastBox();
        assertEquals(-1800000000, box.getLeft());
        assertEquals(-850511287, box.getBottom());
        assertEquals(1800000000, box.getRight());
        assertEquals(850511287, box.getTop());
        storage.addBoundingBox(box1);
        assertEquals(1, storage.getBoundingBoxes().size());
        storage.deleteBoundingBox(box1);
        assertEquals(0, storage.getBoundingBoxes().size());
    }

    /**
     * Test returning all elements in storage
     */
    @Test
    public void getElements() {
        assertEquals(286110, storage.getElements().size());
    }

    /**
     * Test returning all nodes belonging to way in storage
     */
    @Test
    public void getWayNodes() {
        List<Node> wayNodes = storage.getWayNodes();
        assertEquals(245776, wayNodes.size());
        Node node = (Node) storage.getOsmElement(Node.NAME, 300852915L);
        assertTrue(wayNodes.contains(node));
    }

    /**
     * Test that changing an id and rehashing works
     */
    @Test
    public void rehash() {
        Node node = (Node) storage.getOsmElement(Node.NAME, 300852915L);
        assertNotNull(node);
        node.setOsmId(-12345);
        assertNull(storage.getOsmElement(Node.NAME, -12345L));
        storage.rehash();
        Node changed = (Node) storage.getOsmElement(Node.NAME, -12345L);
        assertNotNull(changed);
        assertEquals(node, changed);
    }

    private class LineCounter extends PrintStream {
        int lineCount = 0;

        /**
         * Create a PrintStream that simply counts println ops
         * 
         * @param out an existing OutputStream
         */
        public LineCounter(OutputStream out) {
            super(out, true);
        }

        @Override
        public void print(String s) {// do what ever you like
            fail();
        }

        @Override
        public void println(String s) {// do what ever you like
            lineCount++;
        }
    }

    /**
     * Check that logStorage outputs something
     */
    @Test
    public void logStorage() {
        PrintStream savedOut = System.out;
        try {
            LineCounter counter = new LineCounter(System.out);
            System.setOut(counter);
            storage.logStorage();
            assertEquals(732534, counter.lineCount);
        } finally {
            System.setOut(savedOut);
        }
    }
}
