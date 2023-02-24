package BPlusTree;

import java.util.ArrayList;
import java.util.Collections;

public class Node {
    private InternalNode internalNode;
    private ArrayList<Integer> keys;
    private boolean isRootNode;
    private boolean isLeafNode;

    public Node(){
        isRootNode = true;
        isLeafNode = true;
        keys = new ArrayList<>();
    }

    public InternalNode getInternalNode(){
        return internalNode;
    }

    public void setInternalNode(InternalNode internalNode){
        this.internalNode = internalNode;
    }

    public ArrayList<Integer> getKeys(){
        return keys;
    }

    public int getKey(int i){
        return keys.get(i);
    }

    //set the key of the node
    public int setKey(int key){
        if (keys.size() == 0) {
            keys.add(key);
            return 0;
        }
        keys.add(key);
        Collections.sort(keys);
        int low = 0;
        int high = keys.size() - 1;
        int index = -1;

        // Finds the index position of the key
        // If there are duplicate keys, then the left most key will be taken
        while (low <= high) {
            // Divides by 2
            int mid = (low + high) >>> 1;
            int valMid = keys.get(mid);

            if (valMid < key) {
                low = mid + 1;
            } else if (valMid > key) {
                high = mid - 1;
            } else {
                index = mid;
                low = mid + 1;
            }
        }
        return index;
    }

    public boolean getIsLeafNode() {
        return isLeafNode;
    }

    public void setIsLeafNode(boolean isitLeafNode) {
        isLeafNode = isitLeafNode;
    }

    public boolean getIsRootNode() {
        return isRootNode;
    }

    public void setIsRootNode(boolean isitRootNode) {
        isRootNode = isitRootNode;
    }

    //delete key in the node
    public void doKeyDeletion(int index) {
        keys.remove(index);
    }

    public void doKeysDeletion() {
        keys = new ArrayList<>();
    }

    //find the smallest key of the node
    public int doSmallestKeyRetrieval(){
        int smallestKey;
        InternalNode intNode;

        //if is a leaf, the smallest key will be the first key in the node
        if (isLeafNode){
            smallestKey = this.getKey(0);
        }
        else {

            intNode = (InternalNode) this;

            // When leaf node is not reached, continue searching until you are the parent node of a leaf node
            while (!intNode.getChildNode(0).getIsLeafNode()){
                intNode = (InternalNode) intNode.getChildNode(0);
            }
            smallestKey = intNode.getChildNode(0).getKey(0);
        }
        return smallestKey;
    }

    //delete the node
    public void doNodeDeletion(){
        //if it has a internalNode, delete yourself and set your internalNode as null
        if (internalNode != null){
            internalNode.doChildNodeDeletion(this);
            internalNode = null;
        }
        // if yourself is a leaf node
        if (isLeafNode){
            LeafNode leafNode = (LeafNode) this;
            leafNode.deleteAddresses(); //delete all the addresses
            leafNode.setNextNode(null); //set your next leaf as null
        }
        // if yourself is an internal node
        else{
            InternalNode intNode = (InternalNode) this;
            intNode.doAllChildNodesDeletion();
        }
        isRootNode = true;
        isLeafNode = true;
        keys = new ArrayList<>();
    }
}
