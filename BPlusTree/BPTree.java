package BPlusTree;

import Storage.Address;
import Storage.Record;

import java.util.ArrayList;

public class BPTree {
    // Pointer size is 6 because 64 bits used for addressing ------> 2^6 = 64
    private static final int POINTER_SIZE = 6;
    // For numVotes
    private static final int KEY_SIZE = 4;
    Node root;
    int noOfLevels;
    int noOfNodes;
    int noOfNodesDeleted;
    int maxNoOfKeys;
    int minNoOfInternalKeys;
    int minNoOfLeafKeys;

    public BPTree(int sizeOfBlock) {
        //n
        maxNoOfKeys = (sizeOfBlock - POINTER_SIZE) / (POINTER_SIZE + KEY_SIZE);
        minNoOfInternalKeys = (int) Math.floor(maxNoOfKeys / 2);
        minNoOfLeafKeys = (int) Math.floor((maxNoOfKeys + 1) / 2);

        root = createRoot();
        noOfNodes = 0;
        noOfNodesDeleted = 0;

        System.out.println();
        System.out.println("Doing Calculations...");
        System.out.println("Block Size: " + sizeOfBlock + "B");
        System.out.println("Max no. of keys in a node: " + maxNoOfKeys);
        System.out.println("Min no. of keys in an internal node: " + minNoOfInternalKeys);
        System.out.println("Min no. of keys in a leaf node: " + minNoOfLeafKeys);
        System.out.println();
    }

    // create initial root
    public LeafNode createRoot() {
        LeafNode initRoot = new LeafNode();
        noOfLevels = 1;
        noOfNodes = 1;
        initRoot.setIsRootNode(true);
        return initRoot;
    }

    public void doBPTreeInsertion(int key, Address address) {
        this.doLeafNodeInsertion(this.doLeafNodeSearch(key), key, address);
    }

    //The root node is the internal node
    // Retrieves a leaf node that the range of the key falls into
    public LeafNode doLeafNodeSearch(int key) {
        if (this.root.getIsLeafNode())
            return (LeafNode) root;

        ArrayList<Integer> keys;
        InternalNode internalNode = (InternalNode) root;

        keys = internalNode.getKeys();

        int i;

        // Checks if there is a key in the internal node smaller than the input key
        for (i = 0; i < keys.size(); i++) {
            if (key < keys.get(i)) {
                break;
            }
        }

        // Retrieves the child node of the internal node
        Node child = internalNode.getChildNode(i);

        // If the node is a left node, return the node
        if (child.getIsLeafNode()) {
            return (LeafNode) child;
        } else {
            return doLeafNodeSearch((InternalNode) child, key);
        }

    }


    public LeafNode doLeafNodeSearch(InternalNode internalNode, int key) {
        ArrayList<Integer> keys = internalNode.getKeys();
        int i;

        for (i = 0; i < keys.size(); i++) {
            if (key < keys.get(i)) {
                break;
            }
        }

        Node child = internalNode.getChildNode(i);
        if (child.getIsLeafNode()) {
            return (LeafNode) child;
        } else {
            return doLeafNodeSearch((InternalNode) child, key);
        }

    }

    public void doLeafNodeInsertion(LeafNode leafNode, int key, Address address) {
        try {
            if (leafNode.getKeys().size() >= maxNoOfKeys) {
                doLeafNodeSeparation(leafNode, key, address);
            } else {
                leafNode.setAddress(key, address);
            }
        } catch (Error e) {
            System.out.println(e.getMessage());
        }

    }

    public void doLeafNodeSeparation(LeafNode prevLeaf, int key, Address address) {
        //create local arrays for addresses and keys to store original + new key to insert
        //this is why the size of both arrays are maxNoOfKeys+1
        Address addresses[] = new Address[maxNoOfKeys + 1];
        int keys[] = new int[maxNoOfKeys + 1];
        //create a brand-new leaf node for separation
        LeafNode newLeaf = new LeafNode();

        //inserting old leaf's keys into the arrays that are created previously
        int i;
        for (i = 0; i < maxNoOfKeys; i++) {
            keys[i] = prevLeaf.getKey(i);
            addresses[i] = prevLeaf.getAddress(i);
        }
        /**
         * compare from the tail of the keys, if key >= keys[i], put key to the right side
         * else shift keys that are bigger than key to the back, then insert the key to the correct position
         */
        for (i = maxNoOfKeys - 1; i >= 0; i--) {

            if (keys[i] <= key) {
                i++;
                keys[i] = key;
                addresses[i] = address;
                break;
            }

            keys[i + 1] = keys[i];
            addresses[i + 1] = addresses[i];
        }

        //erasing values(keys and addresses) inside old leaf
        prevLeaf.doSeparation();

        //re-insert the latest keys and addresses from local array into the two leaf nodes
        for (i = 0; i < minNoOfLeafKeys; i++)
            prevLeaf.setAddress(keys[i], addresses[i]);

        //continue to insert into new leaf
        for (i = minNoOfLeafKeys; i < maxNoOfKeys + 1; i++)
            newLeaf.setAddress(keys[i], addresses[i]);

        //adjust the pointers of the leaf nodes
        //set old leaf node to point to new leaf node
        //set new leaf node to point to next leaf node
        newLeaf.setNextNode(prevLeaf.getNextNode());
        prevLeaf.setNextNode(newLeaf);

        //setting parents for new leafnode
        if (prevLeaf.getIsRootNode()) { //if the leaf node is the only node in the tree, and thus it is a root

            InternalNode newRoot = new InternalNode(); //Every 2 nodes need a parent nodes, thus we need a new node
            prevLeaf.setIsRootNode(false); // No longer a root node
            newRoot.setIsRootNode(true); // New node become the root node
            newRoot.doChildInsertion(prevLeaf); //Add left child
            newRoot.doChildInsertion(newLeaf); //Add right child
            root = newRoot;
            noOfLevels++;
        } else if (prevLeaf.getInternalNode().getKeys().size() < maxNoOfKeys) {
            prevLeaf.getInternalNode().doChildInsertion(newLeaf);
        } else
            doParentSeparation(prevLeaf.getInternalNode(), newLeaf);

        // updating nodeCount
        noOfNodes++;
    }

    public void doParentSeparation(InternalNode parentNode, Node childNode) {

        Node childNodes[] = new Node[maxNoOfKeys + 2];
        int keys[] = new int[maxNoOfKeys + 2];
        int key = childNode.doSmallestKeyRetrieval();
        InternalNode parentNode2 = new InternalNode();

        parentNode2.setIsRootNode(false);

        // getting full and sorted lists of keys and children
        for (int i = 0; i < maxNoOfKeys + 1; i++) {
            childNodes[i] = parentNode.getChildNode(i);
            keys[i] = childNodes[i].doSmallestKeyRetrieval();
        }

        for (int i = maxNoOfKeys; i >= 0; i--) {
            if (keys[i] <= key) {
                i++;
                keys[i] = key;
                childNodes[i] = childNode;
                break;
            }

            keys[i + 1] = keys[i];
            childNodes[i + 1] = childNodes[i];
        }

        //clearing old parent values
        parentNode.doSeparation();

        for (int i = 0; i < minNoOfInternalKeys + 2; i++)
            parentNode.doChildInsertion(childNodes[i]);
        for (int i = minNoOfInternalKeys + 2; i < maxNoOfKeys + 2; i++)
            parentNode2.doChildInsertion(childNodes[i]);

        //setting parent for the new parent node
        if (parentNode.getIsRootNode()) {

            InternalNode newRoot = new InternalNode();
            parentNode.setIsRootNode(false);
            newRoot.setIsRootNode(true);
            newRoot.doChildInsertion(parentNode);
            newRoot.doChildInsertion(parentNode2);
            root = newRoot;
            noOfLevels++;

        } else if (parentNode.getInternalNode().getKeys().size() < maxNoOfKeys)
            parentNode.getInternalNode().doChildInsertion(parentNode2);

        else
            doParentSeparation(parentNode.getInternalNode(), parentNode2);

        // updating nodeCount
        noOfNodes++;
    }

    //remove the key from the leaf node
    public ArrayList<Address> doKeyRemoval(int key) {
        ArrayList<Integer> keys;
        LeafNode leafNode;
        ArrayList<Address> addressList = new ArrayList<>();

        ArrayList<Address> returnAddressListToDelete = doRecordsWithKeysRetrieval(key, false);

        int length = doRecordsWithKeysRetrieval(key, false).size();

        //Searching the records with the given key value
        for (int j = 0; j < length; j++) {
            leafNode = doLeafNodeSearch(key);
            keys = leafNode.getKeys();
            for (int i = 0; i < keys.size(); i++) {
                if (keys.get(i) == key) {
                    leafNode.deleteAddress(i);
                    if (!leafNode.getIsRootNode()) {
                        doLeafCleaning(leafNode);
                        addressList.addAll(leafNode.getAddresses());
                    }
                    break;
                }
            }
        }

        System.out.println("No of nodes deleted: " + noOfNodesDeleted);

        noOfNodes -= noOfNodesDeleted;

        return returnAddressListToDelete;
    }

    //When the leaf node does not meet the min no of keys requirement
    public void doLeafCleaning(LeafNode leafNode) {

        //If no need to change node, reset parent and finish
        if (leafNode.getKeys().size() >= minNoOfLeafKeys) {
            doParentNodeCleaning(leafNode.getInternalNode());
            return;
        }

        //No of keys required since current size is less than minimum No of key (Floor of (N + 1)/2)
        int required = minNoOfLeafKeys - leafNode.getKeys().size();
        int leftExcess = 0;
        int rightExcess = 0;
        LeafNode left = (LeafNode) leafNode.getInternalNode().getLeftSiblingNode(leafNode);
        LeafNode right = (LeafNode) leafNode.getInternalNode().getRightSiblingNode(leafNode);
        InternalNode copy;

        //Getting the extra No of keys left/right sibling node can give
        if (left != null) leftExcess += left.getKeys().size() - minNoOfLeafKeys;
        if (right != null) rightExcess += right.getKeys().size() - minNoOfLeafKeys;

        //If it is possible to borrow from neighbouring nodes
        if (leftExcess + rightExcess >= required) {
            if (left != null) {
                leafNode.setAddress(left.getKey(left.getKeys().size() - 1), left.getAddress(left.getKeys().size() - 1));
                left.deleteAddress(left.getKeys().size() - 1);
            } else {
                leafNode.setAddress(right.getKey(0), right.getAddress(0));
                right.deleteAddress(0);
            }

            copy = leafNode.getInternalNode();
        }

        //A merge is required as we are unable to borrow from neighbouring nodes
        else {
            if (left != null) {
                for (int i = 0; i < leafNode.getKeys().size(); i++) {
                    left.setAddress(leafNode.getKey(i), leafNode.getAddress(i));
                }
            } else {
                for (int i = 0; i < leafNode.getKeys().size(); i++) {
                    right.setAddress(leafNode.getKey(i), leafNode.getAddress(i));
                }
            }

            //This is for resetting the parent after deleting
            copy = leafNode.getInternalNode();

            if (left == null) {
                if (!copy.getIsRootNode()) {
                    left = doLeafNodeSearch(copy.doSmallestKeyRetrieval() - 1);
                }
            }

            //Pointer will be changed to the right node
            left.setNextNode(leafNode.getNextNode());


            //removes the leaf node in the B+ tree
            leafNode.doNodeDeletion();
            noOfNodesDeleted++;
        }

        //Updating the parent
        doParentNodeCleaning(copy);
    }

    public void doParentNodeCleaning(InternalNode parent) {
        if (parent.getIsRootNode()) {

            // if root has at least 2 children, reset and return
            if (parent.getChildNodes().size() > 1) {

                //resetting the parent
                Node child = parent.getChildNode(0);
                parent.doChildNodeDeletion(child);
                parent.doChildInsertion(child); //will find the smallest key in the parent after adding back, which will update the parent
                return;
            }

            // if root has only 1 child, delete root level
            else {

                //setting the only child as root
                root = parent.getChildNode(0);
                parent.getChildNode(0).setIsRootNode(true);

                parent.doNodeDeletion();
                noOfNodesDeleted++;
                noOfLevels--;
                return;
            }
        }

        int required = minNoOfInternalKeys - parent.getKeys().size();
        int leftExcess = 0;
        int rightExcess = 0;

        InternalNode leftSiblingNode = (InternalNode) parent.getInternalNode().getLeftSiblingNode(parent);
        InternalNode rightSiblingNode = (InternalNode) parent.getInternalNode().getRightSiblingNode(parent);
        InternalNode duplicate;

        if (leftSiblingNode != null)
            leftExcess += leftSiblingNode.getKeys().size() - minNoOfInternalKeys;

        if (rightSiblingNode != null)
            rightExcess += rightSiblingNode.getKeys().size() - minNoOfInternalKeys;

        //If there are extras from the left or right, then we borrow
        if (required <= leftExcess + rightExcess) {
            if (leftSiblingNode != null) {
                for (int i = 0; i < required; i++) {
                    parent.insertChildToFront(leftSiblingNode.getChildNode(leftSiblingNode.getChildNodes().size() - 1));
                    leftSiblingNode.doChildNodeDeletion(leftSiblingNode.getChildNode(leftSiblingNode.getChildNodes().size() - 1));
                }

            } else {
                for (int i = 0; i < required; i++) {
                    parent.doChildInsertion(rightSiblingNode.getChildNode(0));
                    rightSiblingNode.doChildNodeDeletion(rightSiblingNode.getChildNode(0));
                }
            }
            duplicate = parent.getInternalNode();
        }

        //There are insufficient extra keys for borrowing, we merge.
        else {
            // If there is vacancy for right node
            if (leftSiblingNode == null) {
                for (int i = 0; i < parent.getChildNodes().size(); i++) {
                    rightSiblingNode.doChildInsertion(parent.getChildNode(i));
                }
            }

            // If there is vacancy for left node
            else {
                for (int i = 0; i < parent.getChildNodes().size(); i++) {
                    leftSiblingNode.doChildInsertion(parent.getChildNode(i));
                }
            }

            // After merging, we delete the node
            duplicate = parent.getInternalNode();

            //removes the parent node
            parent.doNodeDeletion();
            noOfNodesDeleted++;
        }
        doParentNodeCleaning(duplicate);
    }


    // Code for Experiment 2
    public void showExperiment2() {

        System.out.println("The parameter n of the B+ tree: " + this.maxNoOfKeys);
        System.out.println("The No of nodes of the B+ tree: " + this.noOfNodes);
        System.out.println("The No of levels of the B+ tree: " + this.noOfLevels);
        System.out.println("The content of the root node (only the keys): ");
        InternalNode rootDuplicate = (InternalNode) root; //to get the root node
        System.out.println(rootDuplicate.getKeys().toString());
    }

    public ArrayList<Address> showExperiment3(int searchingKey) {
        return doRecordsWithKeysRetrieval(searchingKey, true);
    }


    private ArrayList<Address> doRecordsWithKeysRetrieval(int searchingKey, boolean isPrint) {
        ArrayList<Address> result = new ArrayList<>();
        int blockAccess = 1; // access the root
        int siblingAccess = 0;
        ArrayList<Address> recordsAddressList = new ArrayList<>();

//        if (isPrint) {
//            System.out.printf("access root and nodes accesses: %d, contents of the root node: %s\n", blockAccess, this.root.getKeys().toString());
//        }
        Node currNode = root;
        InternalNode internalNode;
        // searching for leaf node with key
        while (!currNode.getIsLeafNode()) { //the mount of node access we need to reach to leaf node
            internalNode = (InternalNode) currNode;
            for (int i = 0; i < internalNode.getKeys().size(); i++) {
                if (searchingKey <= internalNode.getKey(i)) {
                    currNode = internalNode.getChildNode(i); //look deeper into bottom left (smallest) child
                    blockAccess++;
//                    if (isPrint && blockAccess <= 5) { //to prevent terminal output when deleting nodes
//                        System.out.printf("Go to child node [%d], current key [%d], searching key [%d] node accessed: %d\n", i, internalNode.getKey(i), searchingKey, blockAccess);
//                        System.out.printf("Content of the index node: %s\n", currNode.getKeys().toString());
//                    }
                    break;
                }
                if (i == internalNode.getKeys().size() - 1) { //if no smaller key can be found, mean look at the right child node
                    currNode = internalNode.getChildNode(i + 1);
                    blockAccess++;
//                    if (isPrint && blockAccess <= 5) {
//                        System.out.printf("Go to child node [%d], current key [%d], searching key [%d] node accessed: %d\n", i, internalNode.getKey(i), searchingKey, blockAccess);
//                        System.out.printf("Content of the index node: %s\n", currNode.getKeys().toString());
//                    }

                    break;
                }
            }
        }
        // after leaf node is found, find all records with same key
        LeafNode curr = (LeafNode) currNode;
        boolean finish = false;
        // compare the keys in the leaf node and the searching key
        while (!finish && curr != null) {
            // finding same keys within leaf node
//            if (isPrint && blockAccess <= 5) {
//                blockAccess++;
//                System.out.printf("Content of the leaf node: %s, node accessed: %s\n", curr.getKeys().toString(), blockAccess);
//            }

            for (int i = 0; i < curr.getKeys().size(); i++) {
                // found same key, add into result list
                if (curr.getKey(i) == searchingKey) {
                    // add the keys
                    result.add(curr.getAddress(i));
                    continue;
                }
                // if curKey > searching key, no need to continue searching
                if (curr.getKey(i) > searchingKey) {
                    finish = true;
                    break;
                }
            }
            if (!finish) {
                // trying to check sibling node has remaining records of same key
                // replace the curr node var with the next node
                if (curr.getNextNode() != null) {
                    curr = curr.getNextNode();
                    blockAccess++;
                    siblingAccess++;
                } else {
                    break;
                }
            }
        }
        if (isPrint) {
//            if (siblingAccess > 0) {
//                System.out.println("the No of nodes accesses in siblings: " + siblingAccess);
//            }
            System.out.println("Searching numOfVotes = " + searchingKey + " the No of records accessed = " + result.size());

            System.out.println("B+ tree");
            System.out.println("------------------------------------------------------------------");
            System.out.printf("Total no of index nodes accesses: %d\n", blockAccess);
            System.out.printf("Total no of data block accesses: %d\n", result.size() + blockAccess);
        }


        return result;
    }

    //Experiment 4
    public ArrayList<Address> doRangeRecordsRetrieval(int low, int high) {
        ArrayList<Address> result = new ArrayList<>();
        int nodeCount = 1;
        int siblingCount = 0;

        Node curr = root;
        InternalNode internalNode;

        System.out.printf("access root and nodes accesses: %d, contents of the root node: %s\n", nodeCount, this.root.getKeys().toString());


        while (!curr.getIsLeafNode()) {
            internalNode = (InternalNode) curr;
            int no_of_keys = internalNode.getKeys().size();
            for (int i = 0; i < no_of_keys; i++) {
                if (low <= internalNode.getKey(i)) {
                    curr = internalNode.getChildNode(i);
                    nodeCount++;
                    if (nodeCount <= 5) {
                        System.out.printf("Go to child node [%d], current key [%d], searching key [%d] node accessed: %d \n", i, internalNode.getKey(i), low, nodeCount);
                        System.out.printf("Content of the index node: %s\n", curr.getKeys().toString());

                    }

                    break;
                }

                if (i == no_of_keys - 1) {

                    curr = internalNode.getChildNode(i + 1);
                    nodeCount++;

                    if (nodeCount <= 5) {
                        System.out.printf("Go to child node [%d], current key [%d], searching key [%d] node accessed: %d \n", i, internalNode.getKey(i), low, nodeCount);
                        System.out.printf("Content of the index node: %s\n", curr.getKeys().toString());

                    }

                    break;
                }
            }
        }

        // after leaf node is found, find all records with same key
        LeafNode curLeaf = (LeafNode) curr;
        boolean found = false;
        while (!found && curLeaf != null) {
            // finding same keys within leaf node
            if (nodeCount <= 5) {
                nodeCount++;
                System.out.printf("Content of the leaf node: %s, node accessed: %s\n", curLeaf.getNextNode().getKeys().toString(), nodeCount);

            }
            for (int i = 0; i < curLeaf.getKeys().size(); i++) {
                // found same key, add into result list
                if (curLeaf.getKey(i) >= low && curLeaf.getKey(i) <= high) {
                    result.add(curLeaf.getAddress(i));
                    continue;
                }
                // if curKey > searching key, no need to continue searching
                if (curLeaf.getKey(i) > high) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                // trying to check sibling node has remaining records of same key
                if (curLeaf.getNextNode() != null) {
                    curLeaf = (LeafNode) curLeaf.getNextNode();
                    nodeCount++;
                    siblingCount++;
                } else {
                    break;
                }
            }
        }
        if (siblingCount > 0) {

            System.out.printf("the No of nodes accesses in siblings: %d \n", siblingCount);

        }

        System.out.printf("Searching numOfVotes range of %d - %d, the No of records accessed: %d\n", low, high, result.size());
        System.out.printf("Total No of nodes accesses: %d, total No of block accesses: %d\n", nodeCount, result.size() + nodeCount);
        return result;
    }

}
