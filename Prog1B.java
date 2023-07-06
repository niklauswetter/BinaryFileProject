/* ------------------------------------------------------------------------
 * Program 1 - Creating and Exponentially Searching a Binary File (Part B)
 * ------------------------------------------------------------------------
 * Prog1A.java: This program will take in a binary file that is created with a specified format from Part A of this
 *              project. This program will be able to search the binary file, as well as display a summary of the
 *              different records in different ways.
 *
 *              The file format will expect that each record is the same length and has 9 fields for each individual
 *              record. The file will be expected to have a dummy first record that contains only the length of the name
 *              field in the first int value in the file, the following 8 fields of the dummy record will be spaces or
 *              zero depending on the datatype of the field. All following records begin at index 1.
 *
 *              This program will display records in two different ways, automatically and on demand by the user. For
 *              the automatic display, the first 3, middle 3, and last 3 records of the file should be printed to the
 *              console. If the number of records is even print the middle 2 records. If there are less than 3 we will
 *              print as many as there are to the console 3 times; once for each group. At the end we will display the
 *              total quantity of records.
 *
 *              For the user request display the user will be able to provide zero or more EIA ID values to locate
 *              within the binary file. The program will search for these records using the exponential binary search
 *              algorithm and displau the appropriate fields to the console.
 * ------------------------------------------------------------------------
 *     Author: Niklaus Wetter
 *     Course: CSC 460 - Database Design
 * Instructor: Dr. McCann
 *        TAs: Priya  Kaushik
 *             Aayush Pinto
 *   Due Date: September 7, 2022
 * ------------------------------------------------------------------------
 * Java Version: java version "16.0.2" 2021-07-20
 *               Java(TM) SE Runtime Environment (build 16.0.2+7-67)
 *               Java HotSpot(TM) 64-Bit Server VM (build 16.0.2+7-67, mixed mode, sharing)
 * ------------------------------------------------------------------------
 * Special Compilation Requirements: Must compile Record.java, and include Record.class in ./
 *              Input File Location: /home/cs460/fall22/2021-utility-scale-solar-plants.csv
 * ------------------------------------------------------------------------
 * Missing Features: None yet
 *             Bugs: printRecord() method only works in the printGroups method, does not affect function but is odd
 * ------------------------------------------------------------------------ */

import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Prog1B {

    public static void main(String[] args) {

        RandomAccessFile binFile = null;
        int recordLength = 0;
        int nameLength = 0;
        int numRecords = 0;

        try{
            binFile = new RandomAccessFile(args[0], "r");
            nameLength = binFile.readInt();
            recordLength = 4 + nameLength + 10 + 2 + (5*8); //Length for each field and data type
            numRecords = ((int)binFile.length() / recordLength) - 1; //Length minus the one dummy row
        }catch(Exception e){
            System.out.println("There was a problem opening the file!");
            e.printStackTrace();
        }

        try{
            printGroupsOfRecords(binFile, recordLength, numRecords, nameLength);
            System.out.println("There are "+numRecords+" total records in this file.");
        }catch(Exception e){
            System.out.println("There was a problem printing the record groups!");
            e.printStackTrace();
        }

        try{
            System.out.println();
            Scanner scanner = new Scanner(System.in);
            int searchValue = 0;
            while (searchValue != -1){
                System.out.println("Enter a 5 digit EIA ID to search for records, enter -1 to quit:");
                searchValue = scanner.nextInt();
                if(searchValue == -1)
                    continue;
                searchRecords(searchValue, binFile, recordLength, numRecords, nameLength);
            }
            System.out.println("Search function exited");
        }catch(Exception e){
            System.out.println("There was a problem searching for records!");
            e.printStackTrace();
        }
    }

    /*
     * Method searchRecords(id, binFile, recordLength, numRecords, namelength)
     *
     *        Purpose: This method searches through the binary file to look for a record who's EIA ID field matches the
     *                 id argument supplied to this function. The search algorithm used is exponential binary search,
     *                 essentially finding a range of the file where the record must be and searching only that part. We
     *                 will break this up into two functions with the internal binary search algorithm being implemented
     *                 in it's own function below.
     *
     *  Pre-Condition: The binary file must be properly instantiated and read for this method to work correctly
     *
     * Post-Condition: The record will be printed if it exists in the file, otherwise a message stating it does not
     *                 exist in this file will be displayed.
     *
     *     Parameters:
     *         id -- The EIA ID number to search for
     *         binFile -- The file object to be read from
     *         recordLength -- The length of each individual record line
     *         numRecords -- The total number of records ignoring the dummy first line
     *         nameLength -- the length of the name field in each record
     *
     *        Returns: void
     */
    public static void searchRecords(int id, RandomAccessFile binFile, int recordLength, int numRecords, int nameLength) throws Exception{
        int marker = recordLength; //start after dummy row
        int line = 1;

        int temp = 0;

        binFile.seek(marker);

        temp = binFile.readInt();
        if(temp == id){
            //System.out.println("FOUND block");
            byte[] nameBytes = new byte[nameLength];
            binFile.read(nameBytes, 0, nameLength);
            String name = new String(nameBytes);
            binFile.seek((recordLength * (line + 1)) - 8);
            double ac = binFile.readDouble();
            System.out.printf("[%d][%s][%.2f]\n",id,name,ac);
            return;
        }

        while(line < numRecords && temp < id){
            line *= 2;
            if(line > numRecords)
                line = numRecords;
            marker = line * recordLength;
            binFile.seek(marker);
            temp = binFile.readInt();
            /*
            if(temp == id)
                System.out.println(temp+" FOUND on line " + line);
            else
                System.out.println(temp + " SEEN on line " + line);

             */
        }

        //System.out.println(line/2 +" "+ line);

        int start = line/2;
        int end = line;

        binarySearchRecords(id, binFile, recordLength, numRecords, nameLength, start, end);
    }

    /*
     * Method binarySearchRecords(id, binFile, recordLength, numRecords, namelength, start, end)
     *
     *        Purpose: This method searches through the binary file to look for a record who's EIA ID field matches the
     *                 id argument supplied to this function. This is the internal binary search method for the
     *                 exponential search method we are creating. This is a modified version of basic binary search that
     *                 can search our binary file and print the appropriate message.
     *
     *  Pre-Condition: The binary file must be properly instantiated and read for this method to work correctly, and it
     *                 can only be effectively called from inside the exponential binary search method which finds the
     *                 range to run this on
     *
     * Post-Condition: The record will be printed if it exists in the file, otherwise a message stating it does not
     *                 exist in this file will be displayed.
     *
     *     Parameters:
     *         id -- The EIA ID number to search for
     *         binFile -- The file object to be read from
     *         recordLength -- The length of each individual record line
     *         numRecords -- The total number of records ignoring the dummy first line
     *         nameLength -- the length of the name field in each record
     *         start -- the index to start searching at
     *         end -- the index to stop searching at
     *
     *        Returns: void
     */
    public static void binarySearchRecords(int id, RandomAccessFile binFile, int recordLength, int numRecords, int nameLength, int start, int end) throws Exception{
        //System.out.println("Search range: "+ start +":"+end);

        if(end >= start){
            int middle = ((end-start)/2) + start; //line number
            int marker = middle * recordLength;
            binFile.seek(marker);

            int temp = binFile.readInt();

            //Not sure why my method didnt work here but if i do not debug this works
            if(temp == id) {
                //System.out.println("FOUND " + id + " at line " + middle);
                byte[] nameBytes = new byte[nameLength];
                binFile.read(nameBytes, 0, nameLength);
                String name = new String(nameBytes);
                binFile.seek((recordLength * (middle + 1)) - 8);
                double ac = binFile.readDouble();
                System.out.printf("[%d][%s][%.2f]\n",id,name,ac);
                return;
            }

            if(temp > id) {
                binarySearchRecords(id, binFile, recordLength, numRecords, nameLength, start, middle - 1);
                return;
            }

            binarySearchRecords(id, binFile, recordLength, numRecords, nameLength, middle + 1, end);
            return;
        }

        System.out.println("EIA ID not found!");
        return;
    }

    /*
     * Method printGroupsOfRecords(binFile, recordLength, numRecords)
     *
     *        Purpose: This method will take a binary file and some metadata about it's contents and print the first,
     *                 middle, and last three records as "head" of the file; prints the two middle records if the record
     *                 number total is even.
     *
     *  Pre-Condition: The binary file must be properly instantiated and read for this method to work correctly
     *
     * Post-Condition: The record groups mentioned in the "purpose" section will be printed to the console and the
     *                 binary file will be unchanged.
     *
     *     Parameters:
     *         binFile -- The file object to be read from
     *         recordLength -- The length of each individual record line
     *         numRecords -- The total number of records ignoring the dummy first line
     *
     *        Returns: void
     */
    public static void printGroupsOfRecords(RandomAccessFile binFile, int recordLength, int numRecords, int nameLength) throws Exception{
        //System.out.println("Binary file length: " + binFile.length() + " bytes");
        //System.out.println("Record length: " + recordLength + " bytes");
        //System.out.println("Number of records in file: " + numRecords + " (excluding dummy row)");

        binFile.seek(recordLength); //Seeks the first record starting after the dummy record

        //Behavior for less than 3 records, otherwise continue
        if(numRecords < 3){
            for(int i = 0; i < 3; i++){
                for(int j = 1; j <= numRecords; j++){
                    printRecord(j, binFile, recordLength, nameLength);
                }
            }
            return; //Exit
        }

        for(int i = 1; i < 4; i++){
            printRecord(i, binFile, recordLength, nameLength);
        }

        if(numRecords%2==0){
            //Even print 2
            for(int i = numRecords/2; i < (numRecords/2)+2; i++){
                printRecord(i, binFile, recordLength, nameLength);
            }
        }else{
            //Odd print 3
            for(int i = numRecords/2; i < (numRecords/2)+3; i++){
                printRecord(i, binFile, recordLength, nameLength);
            }
        }

        for(int i = numRecords-2; i <= numRecords; i++){
            printRecord(i, binFile, recordLength, nameLength);
        }
    }

    /*
     * Method printRecord(line, binFile, recordLength, numRecords)
     *
     *        Purpose: This method will take a line number, a binary file, and some metadata from the file to print the
     *                 specified line from the file to the console
     *
     *  Pre-Condition: The binary file must be properly instantiated and read for this method to work correctly and the
     *                 provided line must be inbounds of the file
     *
     * Post-Condition: The specified line will have its id, name, and mw-ac fields printed to the console in a formatted
     *                 string
     *
     *     Parameters:
     *         line -- the line number to print
     *         binFile -- The file object to be read from
     *         recordLength -- The length of each individual record line
     *         numRecords -- The total number of records ignoring the dummy first line
     *
     *        Returns: void
     */
    public static void printRecord(int line, RandomAccessFile binFile, int recordLength, int nameLength) throws Exception{
        binFile.seek(recordLength * line);
        int id = binFile.readInt();
        byte[] nameBytes = new byte[nameLength];
        binFile.read(nameBytes, 0, nameLength);
        String name = new String(nameBytes);
        binFile.seek((recordLength * (line + 1)) - 8);
        double ac = binFile.readDouble();
        System.out.printf("[%d][%s][%.2f]\n",id,name,ac);
    }
}