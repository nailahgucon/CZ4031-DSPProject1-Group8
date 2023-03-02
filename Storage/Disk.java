package Storage;

import Config.Config;

import java.util.ArrayList;
import java.util.Arrays;

public class Disk {
    static final int DISK_SIZE = Config.DISK_CAPACITY;
    int sizeOfDisk;
    int maxBlkSize;
    int blkSize;
    int blkCounts;
    ArrayList<Block> blkList;
    int countOfRecords;

    public Disk(int blkSize) {
        this.sizeOfDisk = DISK_SIZE;
        this.blkSize = blkSize;
        this.maxBlkSize = (int) Math.floor(DISK_SIZE / blkSize);
        this.blkList = new ArrayList<>();
        this.countOfRecords = 0;
        blkCounts = blkList.size();
    }

    public ArrayList<Block> doBlockRetrieval(){
        return this.blkList;
    }


    public int getSizeOfDiskUsed() {
        return blkCounts * blkSize;
    }

    private int getIdOfLastBlk() {
        return blkList.size() > 0 ? blkList.size() - 1 : -1;
    }

    public Address doRecordAppend(Record record) throws Exception {
        int blockId = getIdOfLastBlk();
        return doRecordInsertionAt(blockId, record);
    }

    /**
     doRecordInsertionAt(int blockId, Record record): Inserts a new record into a block.

     Checks if there is an available block to insert the record.
     If the block is full:
        Creates a new block to insert the record
     If there are no available blocks left:
        throws exception
     If an available block found:
        Calls doRecordInsertion() of the Block class to insert the record into the block.
        Increment the countOfRecords counter.
        Returns a new Address object representing the location of the newly inserted record.
     */
    private Address doRecordInsertionAt(int blockId, Record record) throws Exception {
        Block block = null;

        if (blockId>=0){
            block = blkList.get(blockId);
        }

        // unspanned
        // if no available blocks, create a new block to do insertion
        if (block == null || block.isBlockFull()) {
            if (blkList.size() == maxBlkSize) {
                throw new Exception("Insufficient spaces on disk");
            }
            block = new Block(blkSize);
            blkList.add(block);
            blkCounts++;
            blockId = getIdOfLastBlk();
        }
        int offset = block.doRecordInsertion(record);
        countOfRecords++;

        return new Address(blockId, offset);
    }

    /**
     doRecordFetch(Address address): retrieve the record from the address of the block and retrieve the record
     based on the address offset
     */
    public Record doRecordFetch(Address address) {
        return blkList.get(address.blkId).data[address.offset];
    }

    /**
     doRecordDeletion(ArrayList<Address> addressList): Deletes >= 1 record(s) from the database
     It takes an ArrayList of Address objects that identify the location of each record to be deleted.

     For each Address object in the list, it retrieves the block that contains the record to be deleted
     using the block ID stored in the Address object.
     Then it calls the "doRecordDeletionAt" method of the Block object to delete the record at the offset
     specified in the Address object.

     If the deletion is successful (i.e. the specified record is found and deleted):
        decrement countOfRecords counter
        If the block becomes empty (i.e. all records in it have been deleted):
            decrement the blkCounts counter
     If the deletion is unsuccessful:
        display error
     */
    public void doRecordDeletion(ArrayList<Address> addressList) {
        Block block = null;

        try {
            for (Address address : addressList) {
                block = blkList.get(address.blkId);
                // will only return true if the block is empty
                boolean result = block.doRecordDeletionAt(address.offset);
                countOfRecords--;
                if(result){
                    blkCounts--;
                }
            }

        } catch (Error e) {
            System.out.println("Deletion of record unsuccessful: " + e.getMessage());
        }
    }

    /**
     doRecordRetrieval(ArrayList<Address> addressList): Retrieves records from the specified list of disk addresses.

     A blkAccess counter is initialized to 0.
     a recordList array list is created to store the list of retrieved records.
     a blockAccessed array list is created to keep track of which address has already been accessed.

     For each address in the list, it checks if the corresponding block has already been accessed.
     if the corresponding block has NOT been accessed and blkAccess count is less than 5:
        Add blkID to the blockAccessed list.
        Retrieve a block from blkList using the blkId.
        Retrieve an array of records from data array.
        Increment the blkAccess counter.
     Calls doRecordFetch() to retrieve the record at the specified address and adds it to the recordList.
     Returns the list of retrieved records.
     */
    public ArrayList<Record> doRecordRetrieval(ArrayList<Address> addressList) {
        int blkAccess = 0;
        ArrayList<Record> recordList = new ArrayList<>();
        ArrayList<Integer> blockAccessed = new ArrayList<>();

        try {
            for (Address address : addressList) {
                if(!blockAccessed.contains(address.blkId) && blkAccess < 5){
                    blockAccessed.add(address.blkId);
                    Block block = this.blkList.get(address.blkId);
                    Record records[] = block.data;
                    blkAccess++;
                }
                recordList.add(doRecordFetch(address));
            }
        } catch (Error e) {
            System.out.println("Retrieval of records unsuccessful: " + e.getMessage());
        }
        return recordList;
    }
    public int getCurrentBlkCounts() {
        return blkCounts;
    }
    public void showDetails(){
        System.out.println("Num of records: " + countOfRecords);
        System.out.println("Size of a record: " + Storage.Record.size());
        System.out.println("Num of records stored in a block: " + (int) Math.floor(blkSize / Storage.Record.size()));
        System.out.println("Num of blocks for storing the data: " + blkCounts);
    }
}
