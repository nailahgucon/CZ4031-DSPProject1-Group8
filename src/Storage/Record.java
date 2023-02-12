package Storage;

public class Record {
    public String tconst;
    public float averageRating;
    public int numVotes;

    public Record(String tconst, float averageRating, int numVotes) {
        this.tconst = tconst;
        this.averageRating = averageRating;
        this.numVotes = numVotes;
    }

    public String getTconst() {
        return tconst;
    }

    public void setTconst(String tconst) {
        this.tconst = tconst;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public int getNumVotes() {
        return numVotes;
    }

    public void setNumVotes(int numVotes) {
        this.numVotes = numVotes;
    }

    /**
     * Assumptions made:
     * tconst is fixed with 10 chars: 10B, averageRating is float: 4B, numVote is int: 4B
     */

    public static int size(){
        return 18;
    }

    @Override
    public String toString() {
        return "{" + tconst + "; " + averageRating + "; " + numVotes + "}";
    }
}