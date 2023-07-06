/* ------------------------------------------------------------------------
 * Program 2 - Linear Hashing Lite
 * ------------------------------------------------------------------------
 * Prog22.java: This program will read from the binary file database using the created index file to find the records
 *              rather than searching through the entire file to find each one. This program is much simpler than Prog21
 *              as it simply reads values from the disk.
 *
 *              A loop allows the user to input as many EIA ID numbers as they want to search for and will terminate
 *              upon the user entering the value -1. The program will let you know if the value you are searching for is
 *              not present in the databse, and if it is it will display the EIA ID, the name, and the MW-AC solar
 *              capacity in that order.
 * ------------------------------------------------------------------------
 *     Author: Niklaus Wetter
 *     Course: CSC 460 - Database Design
 * Instructor: Dr. McCann
 *        TAs: Priya  Kaushik
 *             Aayush Pinto
 *   Due Date: September 21, 2022
 * ------------------------------------------------------------------------
 * Java Version: java version "16.0.2" 2021-07-20
 *               Java(TM) SE Runtime Environment (build 16.0.2+7-67)
 *               Java HotSpot(TM) 64-Bit Server VM (build 16.0.2+7-67, mixed mode, sharing)
 * ------------------------------------------------------------------------
 * Special Compilation Requirements: None
 * ------------------------------------------------------------------------
 * Missing Features: None yet
 *             Bugs: None yet
 * ------------------------------------------------------------------------ */

import java.io.RandomAccessFile;
import java.util.Scanner;

public class Prog22 {
    public static void main(String[] args){
        RandomAccessFile indexFile = null;
        RandomAccessFile binFile = null;
        int h = 0;
        int binRecordLength = 0;
        int indexRecordLength = 8;
        int numBinRecords = 0;
        int bucketSize = 0;
        int power = 0;

        try{
            indexFile = new RandomAccessFile(args[0], "r");
            binFile = new RandomAccessFile(args[1], "r");
            binRecordLength = binFile.readInt() + 56; //56 extra bytes since the binary file only holds the namelength
            bucketSize = indexFile.readInt();
            h = indexFile.readInt();
            numBinRecords = ((int)binFile.length() / binRecordLength) - 1;
            power = (int)Math.pow(2, h+1);
            //System.out.printf("Index File:\n\tBucket Size: %d\n\tValue of H: %d\n", bucketSize, h);
            //System.out.printf("Binary File:\n\tRecord Length: %d\n\tTotal Records: %d\n", binRecordLength, numBinRecords);
        }catch (Exception e){
            System.out.println("There was a problem opening the binary files!");
            e.printStackTrace();
        }

        try{
            System.out.println("Enter any number of EIA ID values one at a time, use -1 as escape value:");
            Scanner scanner = new Scanner(System.in);
            while(true){

                int search = scanner.nextInt();
                if(search == -1)
                    break;

                int r = search % power;
                boolean found = false;
                for(int i = 0; i < bucketSize; i++){
                    indexFile.seek((r*bucketSize*8) + (i * 8) + 8);
                    if(indexFile.readInt() == search){
                        //Found
                        int location = indexFile.readInt();
                        binFile.seek(binRecordLength*location); //Location starts at one so it skips dummy row
                        int id = binFile.readInt();
                        int nameLength = binRecordLength - 56;
                        byte[] nameBytes = new byte[nameLength];
                        binFile.read(nameBytes, 0, nameLength);
                        String name = new String(nameBytes);
                        binFile.seek((binRecordLength*(location+1))-8);
                        double ac = binFile.readDouble();
                        System.out.printf("[%d][%s][%.2f]\n",id,name,ac);
                        found = true;
                    }
                }

                if(!found){
                    System.out.printf("The target value '%d' was not found.\n", search);
                }
            }
        }catch(Exception e){
            System.out.println("There was a problem printing the index file!");
            e.printStackTrace();
        }
    }
}