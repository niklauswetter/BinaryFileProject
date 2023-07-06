/* ------------------------------------------------------------------------
 * Program 2 - Linear Hashing Lite
 * ------------------------------------------------------------------------
 * Prog21.java: This program will take the path to a binary file with records stores in it and create an index file for
 *              searching the provided file. This index will be stored in the same directory as the program when ran and
 *              will store the information in a file called lhl.idx. The index will use the "EIA ID" field as the key.
 *
 *              The index will not store entire records but rather just the EIA ID value to search for a pointer to that
 *              record's location in the file. This way we only have to search through a few bytes per record rather
 *              than the full size of each one.
 *
 *              The index will use a process we call "linear hashing light" storing records in buckets, each able to
 *              20 index records. The index will be initially composed of 2 empty buckets.
 *
 *              After creating the index the program will display the number of buckets in the index, the number of
 *              records in the lowest occupancy buckets, the number of records in the highest occupancy bucket, and the
 *              mean of the occupanices acorss all buckets.
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
 * Missing Features: None
 *             Bugs: None
 * ------------------------------------------------------------------------ */

import java.io.File;
import java.io.RandomAccessFile;

public class Prog21 {

    public static void main(String[] args){
        RandomAccessFile binFile = null;
        int recordLength = 0;
        int nameLength = 0;
        int numRecords = 0;
        int bucketSize = 20;

        try{
            binFile = new RandomAccessFile(args[0], "r");
            nameLength = binFile.readInt();
            recordLength = 4 + nameLength + 10 + 2 + (5*8); //Length for each field and data type
            numRecords = ((int)binFile.length() / recordLength) - 1; //Length minus the one dummy row
        }catch(Exception e){
            System.out.println("There was a problem opening the binary file!");
            e.printStackTrace();
        }

        //System.out.printf("Filename: %s\nLength of name field: %d\nLength of each record: %d\nTotal number of records: %d\n", args[0], nameLength, recordLength, numRecords);

        //File is opened and the metadata values have been read in, index creation process can now begin

        File file = null; //File pointer for new file
        RandomAccessFile indexFile = null; //Declare file as binary file

        try{
            file = new File("lhl.idx"); //Create new file, delete anything that already exists by same name/extension
            if(file.exists())
                file.delete();

            //Instantiate binary file pointer
            indexFile = new RandomAccessFile(file, "rw");
        }catch(Exception e){
            System.out.println("There was a problem creating the index file!");
            e.printStackTrace();
        }

        //We now have the index file created
        //We have pointers to both the db file and the index file

        //We will write a single 4 byte int value to the beginning of the file which holds the bucket size
        //A second 4 byte int will be written after this which holds the current value of H
        //Each index record will be 8 bytes, an EIA ID field, and a value marking the position of the record in the file

        try{
            /*
             * Since this index file will only ever be used with the bin file it's constructed for we will always have
             * access to the record length from the metadata in that file. In our index file we store the size of each
             * bucket and the current value of H which allows us to both use the correct hash function but also know how
             * many buckets there currently are as well as how much total space is currently avaialble
             */
            indexFile.writeInt(bucketSize); //Store size of buckets
            indexFile.writeInt(0); //H begins at 0
            //Fill first 2 buckets with 'null' values
            indexFile.seek(8);
            for(int i = 0; i < (2*bucketSize); i++){
                indexFile.writeInt(-1); //EIA ID null
                indexFile.writeInt(-1); //Position null
            }
        }catch(Exception e){
            System.out.println("There was a problem writing the dummy line in the index file!");
            e.printStackTrace();
        }

        //Index file now has dummy row of metadata written, contents begin at byte 8
        //First two buckets are created and empty
        try{
            binFile.seek(recordLength);
            for(int i = 0; i < numRecords; i++){
                binFile.seek((recordLength*i) + recordLength);
                indexFile.seek(0);
                int bucketByteSize = indexFile.readInt() * 8;
                int h = indexFile.readInt();
                insertRecord(indexFile, binFile.readInt(), i+1, h, bucketByteSize);
            }
        }catch(Exception e){
            System.out.println("There was a problem writing the records to the index!");
            e.printStackTrace();
        }

        try{
            printIndexData(indexFile);
        }catch (Exception e){
            System.out.println("There was a problem printing the index file data!");
            e.printStackTrace();
        }
    }

    /*
     * Method insertRecord(indexFile, ID, num, h, bucketByteSize)
     *
     *        Purpose: This method will load a record from the binary file into the index file. If the index file does
     *                 not have sufficient space to hold the inserted record this method will expand and rehash the
     *                 index and recursively add the value no matter how many rehashes are required.
     *
     *  Pre-Condition: The binary file must be properly created and the index file must be initialized with bucket size
     *                 and the value of H on the first line, and the buckets must be properly constructed with
     *                 any 'null' values holding an int value of -1
     *
     * Post-Condition: The index file will have all the values from the file loaded into it in proper order in its
     *                 appropriate bucket.
     *
     *     Parameters:
     *         indexFile -- The index file which to write to
     *                ID -- The ID of the record to insert into the index
     *               num -- The location in the binary file this record is at
     *                 h -- The current value of H
     *    bucketByteSize -- The size of each bucket in bytes
     *
     *     Returns: void
     */
    public static void insertRecord(RandomAccessFile indexFile, int ID, int num, int h, int bucketByteSize) throws Exception{
        //Think about doing this recursively perhaps
        int power = (int)Math.pow(2, h + 1);
        int r = ID % power;
        int marker = (r*bucketByteSize) + 8;
        indexFile.seek(marker);
        //We are now at the beginning of the appropriate bucket for the given ID
        int counter = 0;
        while(counter < (bucketByteSize/8)){
            if(indexFile.readInt() == -1){
                //This spot is open
                indexFile.seek(marker + (counter * 8));
                indexFile.writeInt(ID); //Write ID value
                indexFile.writeInt(num); //Write binFile position
                //System.out.printf("ID %d is record number %d written at spot %d in bucket %d\n",ID,num,counter,r);
                return; //Value is stored and we are done
            }else{
                //The value we just checked was taken
                counter++;
                indexFile.seek(marker + (counter * 8));
            }
        }
        //If the method still hasnt returned here the bucket we want to use is full
        //We have to rehash and make our recursive call
        //System.out.println("Bucket full, rehash, recurse!");
        indexFile.seek(4);
        h++;
        indexFile.writeInt(h);
        int toAdd = (int)Math.pow(2, h + 1) - (int)Math.pow(2, h); //Get new number of buckets - number we already had
        //System.out.printf("Buckets to add: %d\n", toAdd);
        indexFile.seek((int)indexFile.length());
        for(int i = 0; i < (toAdd * bucketByteSize / 8); i++){
            indexFile.writeInt(-1); //ID null
            indexFile.writeInt(-1); //Num null
        }
        //The index file is expanded now to the proper amount of buckets
        power = (int)Math.pow(2, h+1);
        for(int i = 0; i < power; i++){
            for(int j = 0; j < bucketByteSize/8; j++){
                indexFile.seek((i*bucketByteSize)+(j*8) + 8);
                int t_id = indexFile.readInt();
                int t_num = indexFile.readInt();
                if(t_id == -1){
                    continue; //This record is null and we can move to next one
                }else{
                    //This record is non-null
                    r = t_id%power;
                    if(r == i){
                        //Stays in the bucket it's already in
                        //System.out.printf("ID: %d will stay in bucket %d\n", t_id, r);
                        continue;
                    }else{
                        //The record must be rehashed to a new bucket
                        //System.out.printf("ID: %d will move to bucket %d\n", t_id, r);
                        indexFile.seek((i*bucketByteSize)+(j*8) + 8);
                        //First erase the record, we already have it's fields in variables in scope above
                        indexFile.writeInt(-1); //Null ID
                        indexFile.writeInt(-1); //Null num
                        //Now relocate record, overflow is impossible in this case
                        indexFile.seek((r*bucketByteSize) + 8); //Go to new bucket
                        int t_counter = 0;
                        while(t_counter < bucketByteSize/8){
                            if(indexFile.readInt() == -1){
                                //This spot is open
                                indexFile.seek((r*bucketByteSize)+(t_counter*8)+8);
                                indexFile.writeInt(t_id);
                                indexFile.writeInt(t_num);
                                //System.out.printf("ID %d is record number %d RE-written at spot %d in bucket %d\n",t_id,t_num,t_counter,r);
                                break;
                            }else{
                                t_counter++;
                                indexFile.seek((r*bucketByteSize)+(t_counter*8)+8);
                            }
                        }
                    }
                }
            }
        }
        /*
         * At this point all the values have been re-hashed appropriately and the index is ready to insert the element
         * that prompted the re-size in the first place. There is a chance still that the index will have to resize
         * again if for example all the items in bucket 1 when h=0 end up in bucket 3 when h=1. If this is the case the
         * method will simply resize the index once more, and try to add the same element in the recursive call below.
         * There will be no duplicate elements because the method returns if the ID is added successfully without resize
         */
        insertRecord(indexFile, ID, num, h, bucketByteSize);
    }

    /*
     * Method printIndexData(indexFile)
     *
     *        Purpose: This method will print the the nbumber of buckets in the index, the minimum and maximum occupancy
     *                 of all the buckets, and the mean occupancy of all buckets, in that order.
     *
     *  Pre-Condition: The index must be completely created and have no faults in it. We expect the index to hold the
     *                 size of each bucket, and the value of h in the first two int values
     *
     * Post-Condition: Data will be printed to the console
     *
     *     Parameters:
     *         indexFile -- The file pointer to the index file we print the data from
     *
     *     Returns: void
     */
    public static void printIndexData(RandomAccessFile indexFile) throws Exception{
        indexFile.seek(0);
        int bucketSize = indexFile.readInt();
        int h = indexFile.readInt();
        int bucketByteSize = bucketSize * 8;
        int power = (int)Math.pow(2, h + 1);

        int minCount = bucketSize;
        int maxCount = 0;
        int sum = 0;

        int counter = 0;
        for(int i = 0; i < power; i++){
            for(int j = 0; j < bucketSize; j++){
                indexFile.seek((i * bucketByteSize) + (j * 8) + 8);
                if(indexFile.readInt() == -1){
                    //Null entry
                    continue;
                }else{
                    counter++;
                }
            }
            if(counter < minCount){
                minCount = counter;
            }else if(counter > maxCount){
                maxCount = counter;
            }
            sum += counter;
            counter = 0;
        }

        System.out.printf("Index file contains %d buckets\n", power);
        System.out.printf("Lowest occupancy bucket contains %d records\n", minCount);
        System.out.printf("Highest occupancy bucket contains %d records\n", maxCount);
        System.out.printf("The mean occupancy across all buckets is %.2f records\n", (double)sum/power);
    }
}