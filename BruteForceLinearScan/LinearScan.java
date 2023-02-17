package BruteForceLinearScan;

import Storage.Address;
import Storage.Block;
import Storage.Record;

import java.util.ArrayList;
import java.util.List;

public class LinearScan {

    ArrayList<Block> dataBlockList;
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


}
