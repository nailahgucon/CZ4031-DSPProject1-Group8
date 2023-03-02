package BruteForceLinearScan;

import Storage.Address;
import Storage.Block;
import Storage.Disk;
import Storage.Record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LinearScan {

    ArrayList<Block> dataBlockList;

    public LinearScan(){

    }
    public LinearScan(ArrayList<Block> blkList) {
        this.dataBlockList = blkList;
    }
//    public Record doRecordFetch(Address address) {
//        return dataBlockList.get(address.getBlkId()).
//    }

    public ArrayList<Record> doLinearScan(int key) {

        System.out.println("\nBrute-force Linear Scan");
        System.out.println("------------------------------------------------------------------");

        int blockAccess = 0;
        ArrayList<Record> recordList = new ArrayList<>();

        for (Block b: dataBlockList) {
            blockAccess++;
            Record[] records = b.doAllRecordRetrieval();

            for (Record r: records) {
                if (r != null && r.getNumVotes() == key) {
                    recordList.add(r);
                }

            }
        }
        System.out.printf("Total no of data block accesses (brute-force linear scan method): %d\n", blockAccess);

        return recordList;

    }

    public ArrayList<Record> doLinearScanRange(int low, int high) {

        System.out.println("\nBrute-force Range Linear Scan");
        System.out.println("------------------------------------------------------------------");

        int blockAccess = 0;
        ArrayList<Record> recordList = new ArrayList<>();

        for (Block b: dataBlockList) {
            blockAccess++;
            Record[] records = b.doAllRecordRetrieval();

            for (Record r: records) {
                if (r != null && r.getNumVotes() >= low && r.getNumVotes() <=high) {
                    recordList.add(r);
                }

            }
        }
        System.out.printf("Total no of data block accesses (brute-force linear scan method): %d\n", blockAccess);

        return recordList;

    }

    public void doLinearScanDeletion(int key, Disk disk) {

        System.out.println("\nBrute-force Linear Scan");
        System.out.println("------------------------------------------------------------------");

        int blockAccess = 0;
        int blkdeleted = 0;
        ArrayList<Address> addressList = new ArrayList<>();
//        ArrayList<Record> recordList = new ArrayList<>();
        int flag = 0;
        int blkid = 0;
        for (Block b: disk.doBlockRetrieval()) {
            blockAccess++;
            Record[] records = b.doAllRecordRetrieval();

            int count = 0;

            for (Record r: records) {
                if (r != null && r.getNumVotes() == key) {
                    flag = 1;
//                    System.out.println(r.getNumVotes());
//                    System.out.println(key);
                    Address add = new Address(blkid,count);
                    addressList.add(add);

                }
                count++;
            }
            if (flag == 1){
                blkdeleted++;
            }
            flag = 0;
            blkid++;
        }

//        System.out.println(addressList.get(0).getBlkId());
//        Address add = new Address(2382,0);
//        Address add = new Address(2382,0);
//        System.out.println(disk.doRecordFetch(addressList.get(0)).getNumVotes());
        disk.doRecordDeletion(addressList);
        int originalDataBlocks = blockAccess;
        int afterDeletionBlocks = disk.getCurrentBlkCounts();
        System.out.printf("Total no of data block accesses (brute-force linear scan method): %d\n", blockAccess);
        System.out.printf("Total no of data block accessed to delete a record (brute-force linear scan method): %d\n", (originalDataBlocks - afterDeletionBlocks));
//        return recordList;

    }



}
