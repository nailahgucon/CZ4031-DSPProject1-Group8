package Storage;

public class Block {
    int maxRecords;
    int currRecords;
    Record[] data;

    public Block(){

    }
    public Block(int size){
        this.currRecords = 0;
        this.maxRecords = (int) Math.floor(size / Record.size());
        this.data = new Record[maxRecords];
    }

    public boolean isBlockFull() {
        return currRecords >= maxRecords;
    }

    // return all the records in the block
    public Record[] doAllRecordRetrieval() {
        return this.data;
    }
    public int doRecordInsertion(Record newRecord) {
        int offset = -1;
        try {
            if (!isBlockFull()) {
                // we can add a record
                for(int i = 0; i < data.length; i++){
                    if(data[i] == null){
                        data[i] = newRecord;
                        currRecords++;
                        offset = i;
                        break;
                    }
                }
            }
        } catch (Error e) {
            System.out.println("Inserting Record Unsuccessful: " + e.getMessage());
        }
        return offset;
    }

    public boolean doRecordDeletionAt(int offset){
        boolean success = false;
        if (data[offset] != null){
            data[offset] = null;
            currRecords--;
            success = true;
        }
        return success;
    }

    public Record doRecordRetrievalAt(int offset){
        return data[offset];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < data.length; i++){
            if (i > 0){
                sb.append(", ");
            }
            sb.append(String.format("%d:{%s}", i, data[i].tconst));
        }
        sb.append("]");
        return sb.toString();
    }
}
