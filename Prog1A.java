/* ------------------------------------------------------------------------
 * Program 1 - Creating and Exponentially Searching a Binary File (Part A)
 * ------------------------------------------------------------------------
 * Prog1A.java: This program is intended to take in a CSV file, read through it and determine some preliminary
 *              information, then use that information to convert the CSV file into a binary file. The field types will
 *              be limited to int, double, and string, making managing our data types pretty simple. Each record in the
 *              file has nine fields of information; our program should be able to handle no records, or very large
 *              numbers of records.
 *
 *              One issue that becomes apparent early is that searching these records will be very difficult without
 *              using a standard size for each of our string fields. The way we will do this is by finding the longest
 *              string in a given field from all records and use that as the maximum string size. We will simply append
 *              space characters to the right of shorter strings until the length is correct. Each string field will
 *              have it's own determined maximum length, not a single value for all string fields. We will have to store
 *              these values somehow in our produced binary file, the assignment handout suggests doing this at the end
 *              of the file so as to keep the data records beginning at index 0.
 *
 *              When dealing with datasets it is assumed there will be quirks within the data, and different edge cases
 *              one must handle. While other circumstances will most likely arise, we will begin by stating that anytime
 *              there is a missing value we simple replace numerical fields with 0, and string fields will conatain the
 *              maximum length of empty space.
 * ------------------------------------------------------------------------
 *     Author: Niklaus Wetter
 *     Course: CSC 460 - Database Design
 * Instructor: Dr. McCann
 *        TAs: Priya  Kaushik
 *             Aayush Pinto
 *   Due Date: August 31, 2022
 * ------------------------------------------------------------------------
 * Java Version: java version "16.0.2" 2021-07-20
 *               Java(TM) SE Runtime Environment (build 16.0.2+7-67)
 *               Java HotSpot(TM) 64-Bit Server VM (build 16.0.2+7-67, mixed mode, sharing)
 * ------------------------------------------------------------------------
 * Special Compilation Requirements: Must compile Record.java, and include Record.class in ./
 *              Input File Location: /home/cs460/fall22/2021-utility-scale-solar-plants.csv
 * ------------------------------------------------------------------------
 * Missing Features: None yet
 *             Bugs: None yet
 * ------------------------------------------------------------------------
 * Progress Log
 *     (8/22/22): Project started, file created, header comment written, learning style guide
 *     (8/24/22): Added Record class, began work on reading file, made documentation more robust
 * ------------------------------------------------------------------------ */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.List;

class Prog1A{

    public static void main(String[] args){
        List<Record> records; //This list will hold all our records to right to the file
        int index = 0; //This variable will be used to get the filename of what we're reading in to save new file

        for(int i = 0; i < args[0].length(); i++){
            if(args[0].charAt(i) == '/')
                index = i;
        }
        String fileName = args[0].substring(index+1, args[0].length()-4)+".bin"; //This var will hold the new filename

        try{
            //Load all records into arraylist
            records = loadRecords(args[0]);
            //Sort in order by EIA ID
            Collections.sort(records);
            //Write each record to file
            writeRecordsToFile(records, fileName);
        } catch (Exception e){
            //Basic error handling for now
            System.out.println("Something went wrong!");
            e.printStackTrace();
        }
    }

    /*
     * Method loadRecords(path)
     *
     *        Purpose: This method will load all of the records from the given CSV file into Record objects and then return
     *                 them all as an arraylist.
     *
     *  Pre-Condition: File has been correctly passed to program as the first and only commandline argument when running
     *
     * Post-Condition: ArrayList variable contains all the records from the given file represented as Record objects
     *
     *     Parameters:
     *         path -- This is the path to the file we will load records from, in this case the first index of args
     *
     *        Returns: An ArrayList of Record objects that represent every record from the CSV file input path. This
     *                 list will not contain any malformed data, or lines that should be exlcuded. This will allow the
     *                 code after this method to simply get the length of the first element to know the max length of
     *                 the project name field.
     */
    public static List<Record> loadRecords(String path) throws FileNotFoundException {
        File f = new File(path); //Single letter variable because it is used solely in the next line to create Scanner
        Scanner scanner = new Scanner(f); //Scanner object to read file from File object above
        List<Record> result = new ArrayList<Record>(); //List object to return, will contain all record objects

        boolean first = true; //Flag used to skip the first line of meta-data

        while(scanner.hasNextLine()){
            if(first){ //Skips the metadata line
                first = false;
                scanner.nextLine();
                continue;
            }
            //Parses each line with another method
            Record temp = parseRecord(scanner.nextLine());
            //Our custom method returns null if the record is bad as detailed by the spec, we then ignore it
            if(temp != null)
                result.add(temp);
        }

        //At this point we have all the records in the array, any that should not be are excluded already
        //Next order of business is to make sure all the Strings are same length

        int nameLength = 0; //Have to find this one
        int codLength = 10; //2 digit month, 2 digit day, 4 digit year, 2 slashes with my formatting choice
        int stateLength = 2; //2 letter state code

        //Find name length
        for(Record r:result){
            if(r.getProjectName().length() > nameLength)
                nameLength = r.getProjectName().length();
        }

        //Fix shorter names
        for(Record r:result){
            //This section fixes the name lengths to be the same length as the longest name
            String temp = r.getProjectName();
            while(temp.length() < nameLength){
                temp += " ";
            }
            r.setProjectName(temp);

            //This section fixes the dates to all be the same length and format
            String date = r.getSolarCOD();
            String[] dateArr = date.split("/",0);
            if(dateArr[0].length() == 1){
                dateArr[0] = "0"+dateArr[0];
            }
            if(dateArr[1].length() == 1){
                dateArr[1] = "0"+dateArr[1];
            }
            date = dateArr[0]+"/"+dateArr[1]+"/"+dateArr[2];
            r.setSolarCOD(date);

            //This section fixes any malformed state fields
            if(r.getState().length() != 2){
                r.setState("  ");
            }
        }

        return result;
    }

    /*
     * Method parseRecord(line)
     *
     *        Purpose: This method will receive a string containing an entry from a csv file which it will parse and use the
     *                 fields from to create a Record object which represents this record line
     *
     *  Pre-Condition: The file has been properly loaded and is being indexed line by line and only a single line is
     *                 passed to this method at once
     *
     * Post-Condition: The given line is returned as a Record object with each of its private fields matching the value
     *                 from the record line passed in
     *
     *     Parameters:
     *         line -- The line from the file to be parsed and represented as a Record object
     *
     *        Returns: A record object containing all the data contained in the passed in record line
     */
    public static Record parseRecord(String line){
        String[] temp = new String[9]; //This array holds each of the fields read in from the csv records
        int charCounter = 0; //Keeps place in the line we're reading in
        int index = 0; //Keeps track of which field we're reading in
        String tempBuffer = ""; //Temporary buffer for String reading

        while(charCounter < line.length() && index < temp.length){

            if(line.charAt(charCounter) == '"'){
                //Check first if quotes, use special logic
                charCounter++; //Move to first char of actual string
                while(line.charAt(charCounter) != '"'){
                    //As long as we dont have another quote
                    tempBuffer += line.charAt(charCounter);
                    charCounter++;
                }
                //At this point tempBuffer has full String loaded into it
                charCounter++; //Move charCounter forward a final time to get off closing quote
                //We're done, the next iteration of the loop will hit comma and add/clear tempBuffer
            } else if(line.charAt(charCounter) == ','){
                //If comma outside quotes, add temp to array and empty it
                temp[index] = tempBuffer;
                tempBuffer = "";
                index++; //Next time this happens we will place in next array index
                charCounter++; //Move to beginning of next field
            } else {
                //Some part of a field
                tempBuffer += line.charAt(charCounter);
                charCounter++;
                //We just continuously load the current field into the buffer until we hit a comma
            }
        }
        temp[index] = tempBuffer; //Add final field to the array when the loop exits

        // AT THIS POINT THE TEMP ARRAY HAS ALL FIELDS AS STRINGS

        //Ignore the line if the EIA ID field is missing
        if(temp[0].isEmpty() || temp[0].length() != 5 || !temp[0].matches("[0-9]+")){
            return null;
        }
        //We leave the 3 string fields as they are, theyll be fixed in main
        //Make sure to fill in any empty doubles as 0.0
        for(int i = 4; i < temp.length; i++){
            if(temp[i].isEmpty()){
                temp[i] = "0.0";
            }
        }

        //Load in values as appropriate data types, using t_ to signify simple temp values
        int t_eid = Integer.parseInt(temp[0]);
        double t_lat = Double.parseDouble(temp[4]);
        double t_lon = Double.parseDouble(temp[5]);
        double t_ghi = Double.parseDouble(temp[6]);
        double t_mwdc = Double.parseDouble(temp[7]);
        double t_mwac = Double.parseDouble(temp[8]);

        //Create object to return
        Record result = new Record(t_eid, temp[1], temp[2], temp[3], t_lat, t_lon, t_ghi, t_mwdc, t_mwac);

        return result;
    }

    /*
     * Method writeRecordsToFile(recordList, fileName)
     *
     *        Purpose: This method will take an arraylist of record objects and write them to a binary file
     *
     *  Pre-Condition: The list must contain all items to be written and they must all be formatted correctly already.
     *                 This means every string field must be the same length, and any necessary lines have been removed.
     *
     * Post-Condition: A file will be populated with the given argument in binary format in the same directory as the
     *                 source file. The binary file will contain the length of the name field in it's first int field
     *                 as the 'id' of a dummy line, all other fields on line one are empty, so data begins at the index
     *                 0 + recordLength
     *
     *     Parameters:
     *         recordList -- The arraylist object of record objects to be written to the file
     *           fileName -- The name of the file that should be saved to the disk
     *
     *     Returns: void
     */
    public static void writeRecordsToFile(List<Record> recordList, String fileName) throws Exception{
        File file = null; //File pointer for new file
        RandomAccessFile binFile = null; //Declare file as binary file

        file = new File(fileName); //Create new file, delete anything that already exists by same name/extension
        if(file.exists())
            file.delete();

        //Instantiate binary file pointer
        binFile = new RandomAccessFile(file, "rw");

        //Get variables to write at end
        int nameLength = recordList.get(0).getProjectName().length();

        //WRITE DUMMY OBJECT, FIRST INT IS NAME LENGTH, EVERYTHING ELSE IS EMPTY
        binFile.writeInt(nameLength);
        String holder = "";
        for(int i = 0; i < nameLength; i++)
            holder += " ";
        binFile.writeBytes(holder); //Name length
        holder = "          ";
        binFile.writeBytes(holder); //Date length
        holder = "  ";
        binFile.writeBytes(holder); //State length
        binFile.writeDouble(0.0);
        binFile.writeDouble(0.0);
        binFile.writeDouble(0.0);
        binFile.writeDouble(0.0);
        binFile.writeDouble(0.0);

        int counter = 0;

        for(Record r:recordList){
            //Write each individual record here
            System.out.println(r);
            binFile.writeInt(r.getEiaID());
            binFile.writeBytes(r.getProjectName());
            binFile.writeBytes(r.getSolarCOD());
            binFile.writeBytes(r.getState());
            binFile.writeDouble(r.getLatitude());
            binFile.writeDouble(r.getLongitude());
            binFile.writeDouble(r.getAverageGHI());
            binFile.writeDouble(r.getSolarCapacityMWDC());
            binFile.writeDouble(r.getSolarCapacityMWAC());


            counter++;

            //DEBUG LINE
            //if(counter == 20 && true)
            //    break;
        }



        binFile.close();


    }
}