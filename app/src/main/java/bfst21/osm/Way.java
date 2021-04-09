package bfst21.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import bfst21.tree.BoundingBox;
import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;


public class Way extends Element implements Drawable, Serializable {

    private static final long serialVersionUID = 3139576893143362100L;
    protected List<Node> nodes = new ArrayList<>();

    private HashMap<String, String> tags;

    public Way(long id) {
        super(id);
    }

    public BoundingBox getBoundingBox() {

        float maxX = nodes.get(0).getX();
        float maxY = nodes.get(0).getY();
        float minX = nodes.get(0).getX();
        float minY = nodes.get(0).getY();

        for (Node node : nodes) {

            float x = node.getX();
            float y = node.getY();

            if (x > maxX) {
                maxX = x;
            }
            if (y > maxY) {
                maxY = y;
            }
            if (x < minX) {
                minX = x;
            }
            if (y < minY) {
                minY = y;
            }
        }
        return new BoundingBox(maxX, maxY, minX, minY);
    }

    public float getX() {
        return nodes.get(nodes.size() / 2).getX();
    }

    public float getY() {
        return nodes.get(nodes.size() / 2).getY();
    }

    private void createTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }
    }

    public void addTag(String key, String value) {
        createTags();
        tags.put(key, value);
    }

    public String getValue(String key) {
        createTags();
        return tags.get(key);
    }

    public HashMap<String, String> getTags() {
        createTags();
        return tags;
    }

    public Node first() {
        return nodes.get(0);
    }

    public Node last() {
        return nodes.get(nodes.size() - 1);
    }

    public void add(Node node) {
        nodes.add(node);
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        gc.moveTo(nodes.get(0).getX(), nodes.get(0).getY());

        int inc = 1;
        if (zoomLevel <= 750) {
            inc = 10;
        } else if (zoomLevel <= 1050) {
            inc = 8;
        } else if (zoomLevel <= 1350) {
            inc = 6;
        } else if (zoomLevel <= 1800) {
            inc = 4;
        } else if (zoomLevel <= 2400) {
            inc = 2;
        }
        for (int i = 0; i < nodes.size(); i += inc) {
            if (i <= nodes.size() - 2) {
                Node node = nodes.get(i);
                gc.lineTo(node.getX(), node.getY());
            }
        }
        int last = nodes.size() - 1;
        gc.lineTo(nodes.get(last).getX(), nodes.get(last).getY());
    }

    public static Way merge(Way first, Way second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        Way merged = new Way(first.getID());
        merged.nodes.addAll(first.nodes);
        merged.nodes.addAll(second.nodes.subList(1, second.nodes.size()));
        return merged;
    }

    public static Way merge(Way before, Way coast, Way after) {
        return merge(merge(before, coast), after);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
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
        if (nodes == null) {
            return other.nodes == null;
        } else {
            return nodes.equals(other.nodes);
        }
    }
}