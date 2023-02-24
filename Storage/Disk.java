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
    // Checks if Disk reaches the max capacity of blocks and checks if block have enough space to add a record,
    //If the block do not have enough space, create a new block and insert the record
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

    // retrieve the record from the address of the block and retrieve the record based on the address offset
    public Record doRecordFetch(Address address) {
        return blkList.get(address.blkId).data[address.offset];
    }

    public void doRecordDeletion(ArrayList<Address> addressList) {
        Block block = null;

        try {
            for (Address address : addressList) {
                block = blkList.get(address.blkId);
//                System.out.println(String.format("Original num of records %s",block.currRecords));
//                System.out.println(Arrays.toString(block.data));
                boolean result = block.doRecordDeletionAt(address.offset);
//                System.out.println(String.format("After deletion num of records %s",block.currRecords));
                countOfRecords--;
                if(result){
                    blkCounts--;
                }
            }

        } catch (Error e) {
            System.out.println("Deletion of record unsuccessful: " + e.getMessage());
        }
    }

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

//                    System.out.printf("Content of Data Blocks [blockId: %d] Accessed: %s\n", address.blkId, Arrays.toString(records));
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
