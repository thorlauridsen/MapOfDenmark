package bfst21.osm;

import java.io.Serializable;
import java.util.Arrays;

import bfst21.models.Util;
import bfst21.tree.BoundingBoxElement;
import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;


public class Way extends BoundingBoxElement implements Drawable, Serializable {

    private static final long serialVersionUID = 3139576893143362100L;
    //private final List<Node> nodes = new ArrayList<>();

    private float[] coords = new float[2];
    private int coordsAmount = 0;

    private ElementType elementType;
    private String role;
    private int maxSpeed = 1;
    private boolean oneWay;
    private boolean oneWayBike;

    public Way(long id) {
        super(id);
    }

    public ElementSize getElementSize() {
        if (elementType.hasMultipleSizes()) {
            double xLength = maxX - minX;
            double yLength = maxY - minY;
            double areaSize = (xLength * yLength * Math.pow(10.0D, 9.0D));
            return ElementSize.getSize(areaSize);
        }
        return ElementSize.DEFAULT;
    }

    public void add(Node node) {
        boolean initialNode = coords.length == 2;

        float[] nodeCoords = node.getCoords();

        if (coordsAmount == coords.length) {
            resizeCoords(coords.length * 2);
        }
        coords[coordsAmount] = nodeCoords[0];
        coords[coordsAmount + 1] = nodeCoords[1];
        coordsAmount += 2;

        updateBoundingBox(nodeCoords, initialNode);
    }

    private void resizeCoords(int capacity) {
        float[] copy = new float[capacity];
        for (int i = 0; i < coordsAmount; i++) {
            copy[i] = coords[i];
        }
        coords = copy;
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        if (elementType == ElementType.ISLAND) {
            System.out.println("--------");
            System.out.println("Start: "+coords[0]+" "+coords[1]);
        }
        gc.moveTo(coords[0], coords[1]);

        //int nodeSkipAmount = getNodeSkipAmount(zoomLevel);
        for (int i = 2; i < (coordsAmount); i += 2) {
            gc.lineTo(coords[i], coords[i + 1]);
            if (elementType == ElementType.ISLAND) {
                System.out.println("lineTo: "+coords[i]+" "+coords[i + 1]);
            }
        }
        //int last = nodes.size() - 1;
        //gc.lineTo(nodes.get(last).getX(), nodes.get(last).getY());
    }

    public static int getNodeSkipAmount(double zoomLevel) {
        if (zoomLevel <= 100) {
            return 10;
        } else if (zoomLevel <= 140) {
            return 9;
        } else if (zoomLevel <= 190) {
            return 8;
        } else if (zoomLevel <= 270) {
            return 7;
        } else if (zoomLevel <= 350) {
            return 6;
        } else if (zoomLevel <= 500) {
            return 5;
        } else if (zoomLevel <= 700) {
            return 4;
        } else if (zoomLevel <= 950) {
            return 3;
        } else if (zoomLevel <= 1350) {
            return 2;
        }
        return 1;
    }

    public static Way reverseMerge(Way first, Way second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        int firstSize = first.getCoordsAmount();
        int secondSize = second.getCoordsAmount();
        int mergedSize = firstSize + secondSize - 2;

        float[] firstCoords = first.getCoords();
        float[] secondCoords = Util.reverseCoordsArray(second.getCoords(), secondSize);

        Way merged = new Way(first.getID());
        merged.coords = new float[mergedSize];
        merged.coordsAmount = mergedSize;

        for (int i = 0; i < firstSize; i++) {
            merged.coords[i] = firstCoords[i];
        }
        for (int i = 2; i < secondSize; i++) {
            int position = i - 2 + firstSize;
            merged.coords[position] = secondCoords[i];
        }
        return merged;
    }

    public static Way merge(Way first, Way second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        int firstSize = first.getCoordsAmount();
        int secondSize = second.getCoordsAmount();
        int mergedSize = firstSize + secondSize - 2;

        float[] firstCoords = first.getCoords();
        float[] secondCoords = second.getCoords();

        Way merged = new Way(first.getID());
        merged.coords = new float[mergedSize];
        merged.coordsAmount = mergedSize;

        for (int i = 0; i < firstSize; i++) {
            merged.coords[i] = firstCoords[i];
        }
        for (int i = 2; i < secondSize; i++) {
            int position = i - 2 + firstSize;
            merged.coords[position] = secondCoords[i];
        }
        return merged;
    }

    public static Way merge(Way before, Way coast, Way after) {
        return merge(merge(before, coast), after);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((coords == null) ? 0 : Arrays.hashCode(coords));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Way other = (Way) obj;
        if (coords == null) {
            return other.coords == null;
        } else {
            return Arrays.equals(coords, other.coords);
        }
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public void setOneWayBike(boolean oneWayBike) {
        this.oneWayBike = oneWayBike;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public ElementType getType() {
        return elementType;
    }

    public void setType(ElementType elementType) {
        this.elementType = elementType;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public float[] getCoords() {
        return coords;
    }

    public int getCoordsAmount() {
        return coordsAmount;
    }

    public Node first() {
        return new Node(coords[0], coords[1], false);
    }

    public Node last() {
        return new Node(coords[coordsAmount - 2], coords[coordsAmount - 1], false);
    }
}
