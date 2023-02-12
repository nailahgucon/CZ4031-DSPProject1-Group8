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

    // CONSTRUCTOR BELOW
    public Disk(int blkSize) {
        this.sizeOfDisk = DISK_SIZE;
        this.blkSize = blkSize;
        this.maxBlkSize = (int) Math.floor(DISK_SIZE / blkSize);
        this.blkList = new ArrayList<>();
        this.countOfRecords = 0;
        blkCounts = blkList.size();
    }

    // FUNCTION
    //calculate the size of disk used
    public int getSizeOfDiskUsed() {
        return blkCounts * blkSize;
    }

    //get the last block's id based on blkList's size
    private int getIdOfLastBlk() {
        return blkList.size() > 0 ? blkList.size() - 1 : -1;
    }

//    //append the record at the last block of the disk
//    public Address doRecordAppend(Record record) throws Exception {
//        int blockId = getIdOfLastBlk();
//        return doRecordInsertionAt(blockId, record);
//    }

    //insert record in a certain block
    private Address doRecordInsertionAt(int blockId, Record record) throws Exception {
        Block block = null;
        //if any block exists
        if (blockId>=0){
            block = blkList.get(blockId);
        }

        // block is not available/not exist, try to create a new block to insert the record
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

    //fetch a record in a block
    public Record doRecordFetch(Address address) {
        return blkList.get(address.blkId).data[address.offset];
    }

    // experiment need to delete multiple records
    public void doRecordDeletion(ArrayList<Address> addressList) {
        Block block = null;

        try {
            for (Address address : addressList) {
                block = blkList.get(address.blkId);
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

    // experiment need to get multiple records
    public ArrayList<Record> doRecordRetrieval(ArrayList<Address> addressList) {
        ArrayList<Record> recordList = new ArrayList<>();
        ArrayList<Integer> blockAccessed = new ArrayList<>();
        int blkAccess = 0;
        try {
            //fetching multiple records and them as a list
            for (Address address : addressList) {

                // for print data blocks only
                if(!blockAccessed.contains(address.blkId) && blkAccess<5){
                    blockAccessed.add(address.blkId);
                    Block block = this.blkList.get(address.blkId);
                    Record records[] = block.data;

                    System.out.printf("Content of Data Blocks [blockId: %d] Accessed: %s\n", address.blkId, Arrays.toString(records));
                    blkAccess++;
                }

                recordList.add(doRecordFetch(address));

            }
        } catch (Error e) {
            System.out.println("Retrieval of records unsuccessful: " + e.getMessage());
        }
        return recordList;
    }

    private int convertToMb(int size_in_byte){
        return size_in_byte/1024/1024;
    }
    public void showDetails(){
        System.out.println("Num of records: " + countOfRecords);
        System.out.println("Num of blocks: " + blkCounts);
        System.out.println("Size of database (in MB): " + convertToMb(this.getSizeOfDiskUsed()));

    }
}
