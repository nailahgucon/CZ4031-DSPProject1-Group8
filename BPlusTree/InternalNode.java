package BPlusTree;

import java.util.ArrayList;

public class InternalNode extends Node {
    private ArrayList<Node> childNodes;

    public InternalNode() {
        super();
        this.setIsLeafNode(false);
        this.setIsRootNode(false);
        childNodes = new ArrayList<Node>();
    }

    public ArrayList<Node> getChildNodes() {
        return childNodes;
    }

    public Node getChildNode(int i) {
        return childNodes.get(i);
    }
    //add child in that internal node
    public int doChildInsertion(Node child) {
        int position = 0;
        // add child at position 0 if there is no childNodes
        if (childNodes.size() == 0) {
            childNodes.add(child);
            child.setInternalNode(this);
            return position;
        }

        //find the smallest key in parent node
        int smallestParentKey = this.doSmallestKeyRetrieval();

        //find the smallest key in child node
        int smallestChildKey = child.doSmallestKeyRetrieval();

        //if the smallest key in child
        if (smallestParentKey <= smallestChildKey){
            position = this.setKey(smallestChildKey);
            this.childNodes.add(position + 1, child);
        }
        else{
            this.setKey(smallestParentKey);
            this.childNodes.add(position, child);
        }

        child.setInternalNode(this);
        return position;
    }
    //add child at front
    public void insertChildToFront(Node child) {
        childNodes.add(0, child);
        child.setInternalNode(this);
        doKeysDeletion();
        //add the new child's key and together with the rest of the keys
        for (int i = 1; i < childNodes.size(); i++) {
            setKey(childNodes.get(i).doSmallestKeyRetrieval());
        }
    }

    public void doSeparation() {
        doKeysDeletion();
        childNodes = new ArrayList<Node>();
    }

    public void doAllChildNodesDeletion() {
        childNodes = new ArrayList<Node>();
    }
    //delete one child from the internal node
    public void doChildNodeDeletion(Node child) {
        childNodes.remove(child);
        doKeysDeletion();

        for (int i = 1; i < childNodes.size(); i++) {
            setKey(childNodes.get(i).doSmallestKeyRetrieval());
        }
    }
    //get the sibling node that is on the left
    public Node getLeftSiblingNode(Node node) {
        if (childNodes.indexOf(node) > 0) {
            return childNodes.get(childNodes.indexOf(node) - 1);
        }
        return null;
    }
    //get the sibling node that is on the right of the node
    public Node getRightSiblingNode(Node node) {
        if (childNodes.indexOf(node) < childNodes.size() - 1) {
            return childNodes.get(childNodes.indexOf(node) + 1);
        }
        return null;
    }
}