package bfst21.tree;

import bfst21.osm.Way;

import java.io.Serializable;
import java.util.List;


public class KdNode implements Serializable {

    private static final long serialVersionUID = 8198499209216068048L;
    private KdNode rightChild;
    private KdNode leftChild;

    private final float x, y;

    private List<Way> list;

    public KdNode(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public List<Way> getList() {
        return list;
    }

    public void setList(List<Way> list) {
        this.list = list;
    }

    public void setRightChild(KdNode kdNode) {
        rightChild = kdNode;
    }

    public void setLeftChild(KdNode kdNode) {
        leftChild = kdNode;
    }

    public KdNode getRightChild() {
        return rightChild;
    }

    public KdNode getLeftChild() {
        return leftChild;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
