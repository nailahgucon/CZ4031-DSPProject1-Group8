package BPlusTree;

import Storage.Address;
import java.util.ArrayList;

public class LeafNode extends Node{
    private ArrayList<Address> addresses;
    private LeafNode nextNode;

    public LeafNode(){
        super();
        setIsRootNode(false);
        setIsLeafNode(true);
        addresses = new ArrayList<Address>();
        setNextNode(null);
    }

    public ArrayList<Address> getAddresses() {
        return addresses;
    }

    public Address getAddress(int i) {
        return addresses.get(i);
    }

    // Sets the address of the current node
    public int setAddress(int key, Address address){
        if (this.getAddresses().size() == 0) {
            this.setKey(key);
            this.addresses.add(address);
            return 0;
        }
        int index = super.setKey(key);
        addresses.add(address);

        for (int i = addresses.size() -2; i >= index; i--)
            addresses.set(i+1, addresses.get(i));

        //set the key index in the current node and the address of the current node
        addresses.set(index, address);
        return index;
    }


    public void deleteAddress(int i) {
        doKeyDeletion(i);
        addresses.remove(i);
    }

    public void deleteAddresses() {
        addresses = new ArrayList<Address>();
    }

    public LeafNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(LeafNode nextNode) {
        this.nextNode = nextNode;
    }

    public void doSeparation(){
        //Delete all keys
        doKeysDeletion();
        //reset all the addresses
        addresses = new ArrayList<Address>();
    }

}
