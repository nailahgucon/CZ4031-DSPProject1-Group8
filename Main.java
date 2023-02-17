import java.text.DecimalFormat;
import java.util.*;

import BPlusTree.BPTree;
import BruteForceLinearScan.LinearScan;
import Storage.Disk;
import Storage.Address;
import Storage.Record;

//imports for buffer reader
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Config.Config;


public class Main implements Config {
    private Disk disk;
    private BPTree BpTree;

    public static List<Record> doRecordReading(String directory) throws FileNotFoundException {
        System.out.println("Loading data in " + directory + " ...");
        File recordFile = new File(directory);
        if (!recordFile.exists()) { //if file not cannot be found,
            throw new FileNotFoundException("File does not exist, Try to change the path of the tsv file in the Config file.");
        }
        System.out.println("Reading data from " + directory + " ...");

        String l;
        String[] category = null; // stores tconst, AverageRating and NumVotes
        BufferedReader reader = null;
        List<Record> records = new ArrayList<>();

        try {
            reader = new BufferedReader(new FileReader(recordFile));
            reader.readLine(); // ignore the first line
            while ((l = reader.readLine()) != null) {
                category = l.split("\\t");
                //split each row of records according to categories
                // combine them into a class
                Record r = new Record(category[0], Float.parseFloat(category[1]), Integer.parseInt(category[2]));
                records.add(r);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println("Total number of records loaded: " + records.size());
        return records;
    }

    public void doBlockCreation(int blkSize) throws Exception {
        disk = new Disk(blkSize); //To allocate the disk space
        BpTree = new BPTree(blkSize); //To create the BPlusTree
        List<Record> data = doRecordReading(DATA_FILE_PATH);

        System.out.println();
        System.out.println("Running program...");
        System.out.println("Inserting the data from the tsv file into the disk and creating the B+ Tree...");

        Address dataAddr;  //Address that the data is going to be stored
        for (Record d : data) {
            dataAddr = disk.doRecordAppend(d); //Insert the records data into disk & retrieve the addresses of the records
            BpTree.doBPTreeInsertion(d.getNumVotes(), dataAddr); //Since we build the B+ tree on the "numVotes" attribute, so we extract the attribute
        }
        System.out.println("Run Successful! The records have been successfully inserted into the disk and the B+ Tree has been created.");
        System.out.println();
    }

    public void runExperiment1() {
        System.out.println("\nRunning Experiment 1...");
        disk.showDetails();
    }

    public void runExperiment2() {
        System.out.println("\nRunning Experiment 2...");
        BpTree.showExperiment2();
    }

    public void runExperiment3() {
        System.out.println("\nRunning Experiment 3...");

        long startTime = System.nanoTime();
        ArrayList<Address> dataAddress = BpTree.showExperiment3(500); // “numVotes” equal to 500 and store them into ArrayList
        ArrayList<Record> records = disk.doRecordRetrieval(dataAddress); // To store all the records fit the condition above

        long runtime = System.nanoTime() - startTime;
        System.out.println("The running time of the retrieval process is " + runtime/1000000 + " ms");

        double averageRate = 0;
        for (Record r : records) {
            averageRate += r.getAverageRating();
        }

        averageRate /= records.size(); //total rating divide by the size of the arraylist to get the average

        System.out.println("The average rating of the records that numVotes = 500 is " + averageRate);


        startTime = System.nanoTime();

        LinearScan ls = new LinearScan(disk.doBlockRetrieval());
        records = ls.doLinearScan(500);

        runtime = System.nanoTime() - startTime;
        System.out.println("The running time of the retrieval process (brute-force linear scan method) is " + runtime/1000000 + " ms");

        averageRate = 0;
        for (Record r : records) {
            averageRate += r.getAverageRating();
        }

        averageRate /= records.size(); //total rating divide by the size of the arraylist to get the average

        System.out.println("The average rating of the records that numVotes = 500 (brute-force linear scan method) is " + averageRate);

    }

    public void runExperiment4() {

    }

    public void runExperiment5() {

    }


    public void displayMenu(int type) throws Exception {
        if (type == 1) { //To select block size
            System.out.println("======================================================================================");
            System.out.println("            << Welcome to Group 8's DSP Project 1 Implementation >>");
            System.out.println();
            System.out.println("What would you like to do?");
            System.out.println("1) Select an experiment \n2) Exit");
            System.out.println("======================================================================================");
            System.out.print("You have selected: ");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();

            switch (input) {
                case "1":
                    doBlockCreation(BLOCK_SIZE_200);
                    break;
                case "2":
                    System.exit(0);
            }
        } else {
            String input;
            do {
                System.out.println("======================================================================================");
                System.out.println("Which experiment would you like to run?");
                System.out.println("Experiment (1): Store the data on the disk & show No. of Records, Size of a Record, No. of Records stored in a Block, and No. of Blocks for storing data.");
                System.out.println("Experiment (2): Build a B+ tree on the attribute ”numVote” by inserting the records sequentially & show the B+ Tree's parameter n value, No. of Nodes, No. of Levels and Root Node Content.");
                System.out.println("Experiment (3): Retrieve movies with the “numVotes” equal to 500 and its required statistics.");
                System.out.println("Experiment (4): Retrieve movies with votes between 30,000 and 40,000 and its required statistics.");
                System.out.println("Experiment (5): Delete movies with the attribute “numVotes” equal to 1,000 and its required statistics.");
                System.out.println("           (exit): Exit ");
                System.out.println("======================================================================================");
                System.out.print("Selection: ");
                Scanner in = new Scanner(System.in);
                input = in.nextLine();
                switch (input) {
                    case "1":
                        runExperiment1();
                        break;
                    case "2":
                        runExperiment2();
                        break;
                    case "3":
                        runExperiment3();
                        break;
                    case "4":
                        runExperiment4();
                        break;
                    case "5":
                        runExperiment5();
                        break;
                }

            } while (!input.equals("exit"));
        }
    }

    //End of MainApp Functions
    public static void main(String[] args) {
        try {
            Main app = new Main();
            app.displayMenu(1);
            app.displayMenu(2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
