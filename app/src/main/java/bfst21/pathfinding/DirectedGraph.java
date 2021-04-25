package bfst21.pathfinding;

import bfst21.models.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;


public class DirectedGraph implements Serializable {

    private static final long serialVersionUID = -2665514385590129687L;

    private int vertexAmount;
    private final HashMap<float[], Integer> coordsToIdMap = new HashMap<>();
    private final TreeMap<Integer, float[]> idToCoordsMap = new TreeMap<>();
    private final TreeMap<Integer, List<Edge>> adjacentEdges = new TreeMap<>();

    public void createVertex(float[] coords, int id) {
        coordsToIdMap.put(coords, id);
        idToCoordsMap.put(id, coords);
        vertexAmount++;
    }

    public int getVertexID(float[] coords) {
        if (coordsToIdMap.containsKey(coords)) {
            return coordsToIdMap.get(coords);
        }
        return -1;
    }

    public float[] getVertexCoords(int id) {
        if (idToCoordsMap.containsKey(id)) {
            return idToCoordsMap.get(id);
        }
        return null;
    }

    public void addEdge(float[] fromCoords, float[] toCoords, int maxSpeed, boolean oneWay) {
        int fromID = getVertexID(fromCoords);
        int toID = getVertexID(toCoords);

        float distance = (float) Util.distTo(fromCoords, toCoords);
        Edge edge1 = new Edge(fromID, toID, distance, maxSpeed);

        addEdge(toID, edge1);
        addEdge(fromID, edge1);

        if (!oneWay) {
            Edge edge2 = new Edge(toID, fromID, distance, maxSpeed);
            addEdge(toID, edge2);
            addEdge(fromID, edge2);
        }
    }

    private void addEdge(int vertexID, Edge edge) {
        List<Edge> edges = new ArrayList<>();
        if (adjacentEdges.containsKey(vertexID)) {
            edges = adjacentEdges.get(vertexID);
        }
        edges.add(edge);
        adjacentEdges.put(vertexID, edges);
    }

    public int getVertexAmount() {
        return vertexAmount;
    }

    public List<Edge> getAdjacentEdges(int vertexID) {
        if (adjacentEdges.containsKey(vertexID)) {
            return adjacentEdges.get(vertexID);
        }
        return new ArrayList<>();
    }

    public TreeMap<Integer, List<Edge>> getAdjacentEdges() {
        return adjacentEdges;
    }
}
