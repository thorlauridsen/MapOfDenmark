package bfst21.test;

import bfst21.models.TransportOption;
import bfst21.models.TransportOptions;
import bfst21.pathfinding.DijkstraPath;
import bfst21.pathfinding.DirectedGraph;
import bfst21.pathfinding.Edge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class DirectedGraphTest {

    @Test
    public void directedGraphTest() {
        DirectedGraph directedGraph = new DirectedGraph();

        float[] coords0 = new float[]{1, 2};
        float[] coords1 = new float[]{2, 1};
        float[] coords2 = new float[]{2, 2};

        directedGraph.createVertex(coords0);
        directedGraph.createVertex(coords1);
        directedGraph.createVertex(coords2);

        directedGraph.addEdge(null, coords0, coords1, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords1, coords2, 10, false, false, false, true, true, true);

        assertEquals(coords0[0], directedGraph.getVertexCoords(0)[0]);
        assertEquals(coords1[0], directedGraph.getVertexCoords(1)[0]);
        assertEquals(coords2[0], directedGraph.getVertexCoords(2)[0]);
        assertEquals(coords0[1], directedGraph.getVertexCoords(0)[1]);
        assertEquals(coords1[1], directedGraph.getVertexCoords(1)[1]);
        assertEquals(coords2[1], directedGraph.getVertexCoords(2)[1]);

        assertEquals(0, directedGraph.getVertexID(coords0));
        assertEquals(1, directedGraph.getVertexID(coords1));
        assertEquals(2, directedGraph.getVertexID(coords2));

        assertEquals(3, directedGraph.getVertexAmount());

        assertEquals(2, directedGraph.getAdjacentEdges(0).size());
        assertEquals(4, directedGraph.getAdjacentEdges(1).size());
        assertEquals(2, directedGraph.getAdjacentEdges(2).size());

        assertEquals(4, directedGraph.getVertices().length);
    }

    @Test
    public void dijkstraHasPathToAllVertices() {
        DirectedGraph directedGraph = new DirectedGraph();

        float[] coords0 = new float[]{1, 2};
        float[] coords1 = new float[]{2, 1};
        float[] coords2 = new float[]{2, 2};
        float[] coords3 = new float[]{2, 3};
        float[] coords4 = new float[]{3, 2};
        float[] coords5 = new float[]{3, 3};
        float[] coords6 = new float[]{9, 9};

        directedGraph.createVertex(coords0);
        directedGraph.createVertex(coords1);
        directedGraph.createVertex(coords2);
        directedGraph.createVertex(coords3);
        directedGraph.createVertex(coords4);
        directedGraph.createVertex(coords5);
        directedGraph.createVertex(coords6);

        directedGraph.addEdge(null, coords0, coords3, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords2, coords1, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords2, coords3, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords2, coords4, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords4, coords5, 10, false, false, false, true, true, true);

        DijkstraPath dijkstraPath = new DijkstraPath(directedGraph, coords0, coords5);
        assertTrue(dijkstraPath.hasPathTo(1));
        assertTrue(dijkstraPath.hasPathTo(2));
        assertTrue(dijkstraPath.hasPathTo(3));
        assertTrue(dijkstraPath.hasPathTo(4));
        assertTrue(dijkstraPath.hasPathTo(5));

        assertFalse(dijkstraPath.hasPathTo(6));
    }

    @Test
    public void dijkstraPathTo_withOneWay_hasCorrectPath(){
        DirectedGraph directedGraph = new DirectedGraph();

        float[] coords0 = new float[]{1, 2};
        float[] coords1 = new float[]{2, 2};
        float[] coords2 = new float[]{3, 2};
        float[] coords3 = new float[]{3, 1};
        float[] coords4 = new float[]{4, 1};
        float[] coords5 = new float[]{4, 2};

        directedGraph.createVertex(coords0);
        directedGraph.createVertex(coords1);
        directedGraph.createVertex(coords2);
        directedGraph.createVertex(coords3);
        directedGraph.createVertex(coords4);
        directedGraph.createVertex(coords5);

        directedGraph.addEdge(null, coords0, coords1, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords1, coords2, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords2, coords3, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords3, coords4, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords4, coords5, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords5, coords2, 10, true, false, false, true, true, true);

        DijkstraPath dijkstraPath = new DijkstraPath(directedGraph, coords0, coords5);
        int counter = 0;
        for (Edge ignored : dijkstraPath.pathTo(5)) {
            counter++;
        }
        boolean actualEdge = counter == 5;

        assertTrue(actualEdge);
    }

    @Test
    public void dijkstraPathTo_withDifferentWeights_hasCorrectPath(){
        DirectedGraph directedGraph = new DirectedGraph();

        //draws an approximate square using real coordinates
        float[] id0 = new float[]{55.69688f,12.43251f}; //SW
        float[] id1 = new float[]{55.69932f,12.43231f}; //NW
        float[] id2 = new float[]{55.69942f,12.43703f}; //NE
        float[] id3 = new float[]{55.69698f,12.43724f}; //SE

        directedGraph.createVertex(id0);
        directedGraph.createVertex(id1);
        directedGraph.createVertex(id2);
        directedGraph.createVertex(id3);

        directedGraph.addEdge(null, id0, id1, 30, false, false, false, true, true, true);
        directedGraph.addEdge(null, id1, id2, 30, false, false, false, true, true, true);
        directedGraph.addEdge(null, id2, id3, 30, false, false, false, true, true, true);
        directedGraph.addEdge(null, id0, id3, 10, false, false, false, true, true, true);

        DijkstraPath dijkstraPath = new DijkstraPath(directedGraph, id0, id3);
        int counter = 0;
        for (Edge ignored : dijkstraPath.pathTo(3)) {
            counter++;
        }
        boolean actualEdge = counter == 3;

        assertTrue(actualEdge);
    }

    @Test
    public void dijkstraPathTo_withBikeAndWalkPath_hasCorrectPath() {
        DirectedGraph directedGraph = new DirectedGraph();
        TransportOptions.getInstance().setCurrentlyEnabled(TransportOption.CAR);

        float[] coords0 = new float[]{1, 2};
        float[] coords1 = new float[]{2, 2};
        float[] coords2 = new float[]{3, 2};
        float[] coords3 = new float[]{3, 1};
        float[] coords4 = new float[]{4, 1};
        float[] coords5 = new float[]{4, 2};

        directedGraph.createVertex(coords0);
        directedGraph.createVertex(coords1);
        directedGraph.createVertex(coords2);
        directedGraph.createVertex(coords3);
        directedGraph.createVertex(coords4);
        directedGraph.createVertex(coords5);

        directedGraph.addEdge(null, coords0, coords1, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords1, coords2, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords2, coords3, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords3, coords4, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords4, coords5, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords5, coords2, 10, false, false, false, false, true, true);

        DijkstraPath dijkstraPath = new DijkstraPath(directedGraph, coords0, coords5);
        int counter = 0;
        for (Edge ignored : dijkstraPath.pathTo(5)) {
            counter++;
        }
        boolean actualEdge = counter == 5;

        assertTrue(actualEdge);
    }

    @Test
    public void dijkstraPathTo_MotorwayVsSecondaryRoad_hasCorrectPath() {

        DirectedGraph directedGraph = new DirectedGraph();
        TransportOptions.getInstance().setCurrentlyEnabled(TransportOption.CAR);

        float[] coords0 = new float[]{1, 2};
        float[] coords1 = new float[]{2, 2};
        float[] coords2 = new float[]{3, 2};
        float[] coords3 = new float[]{3, 1};
        float[] coords4 = new float[]{4, 1};
        float[] coords5 = new float[]{4, 2};

        directedGraph.createVertex(coords0);
        directedGraph.createVertex(coords1);
        directedGraph.createVertex(coords2);
        directedGraph.createVertex(coords3);
        directedGraph.createVertex(coords4);
        directedGraph.createVertex(coords5);

        directedGraph.addEdge(null, coords0, coords1, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords1, coords2, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords2, coords3, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords3, coords4, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords4, coords5, 10, false, false, false, true, true, true);
        directedGraph.addEdge(null, coords5, coords2, 10, false, false, false, false, true, true);

        DijkstraPath dijkstraPath = new DijkstraPath(directedGraph, coords0, coords5);
        int counter = 0;
        for (Edge ignored : dijkstraPath.pathTo(5)) {
            counter++;
        }
        boolean actualEdge = counter == 5;

        assertTrue(actualEdge);
    }




}
