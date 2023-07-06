/* ------------------------------------------------------------------------
 * Program 1 - Creating and Exponentially Searching a Binary File (Part A)
 * ------------------------------------------------------------------------
 * Record.java: This class is going to provide the blueprint for the objects that will store each line we read in from
 *              our input file. The class will have the approapriate fields, methods, and functionality to allow for
 *              easily searching the fields of each record, as well as comparing one record object to others.
 *
 *              This class will need a field for each of the nine different fields from the records themselves. The
 *              methods we need for this class can all retrieve or calculate their information from only the data. We
 *              will include the standard setters and getters, as well as some extra helper methods such as returning
 *              the distance of pertinent string fields.
 *
 *              This class will also implement the Comparable interface which will compare records by their 'EIA ID'
 *              field. This will allow us to easily compare record objects to one another for sorting purposes.
 * ------------------------------------------------------------------------
 *                    Extends: None
 *                 Interfaces: Comparable
 * Public Constants/Variables: None
 *               Constructors: Provides a basic constructor that simply initializes all private fields with passed in
 *                             values. Default constructor will set all values to the default value for each type.
 *              Class Methods: fixProjectNameSpaces(recordList, maxLength)
 *                             fixSolarCODSpaces(recordList, maxLength)
 *           Instance Methods: getters/setters for all private fields
 *                             getProjectNameLength()
 *                             getSolarCODLength()
 *                             compareTo(record)
 *                             toString()
 * ------------------------------------------------------------------------
 *     Author: Niklaus Wetter
 *     Course: CSC 460 - Database Design
 * ------------------------------------------------------------------------
 * Java Version: java version "16.0.2" 2021-07-20
 *               Java(TM) SE Runtime Environment (build 16.0.2+7-67)
 *               Java HotSpot(TM) 64-Bit Server VM (build 16.0.2+7-67, mixed mode, sharing)
 * ------------------------------------------------------------------------ */

import java.util.ArrayList;

public class Record implements Comparable<Record>{

    /*
     * All of these fields store the corresponding field from a given record when read in by the program. It looks much
     * nicer to have the one block comment here rather than all the inline
     */
    private int eiaID;
    private String projectName;
    private String solarCOD;
    private String state;
    private double latitude;
    private double longitude;
    private double averageGHI;
    private double solarCapacityMWDC;
    private double solarCapacityMWAC;

    /*
     * This is a basic constructor for this class, it simply takes one value for each field and assigns it to the new
     * object being created
     */
    public Record(int eiaID, String projectName, String solarCOD, String state, double latitude, double longitude, double averageGHI, double solarCapacityMWDC, double solarCapacityMWAC) {
        this.eiaID = eiaID;
        this.projectName = projectName;
        this.solarCOD = solarCOD;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
        this.averageGHI = averageGHI;
        this.solarCapacityMWDC = solarCapacityMWDC;
        this.solarCapacityMWAC = solarCapacityMWAC;
    }

    //Setters
    public void setEiaID(int eiaID) {this.eiaID = eiaID;}
    public void setProjectName(String projectName) {this.projectName = projectName;}
    public void setSolarCOD(String solarCOD) {this.solarCOD = solarCOD;}
    public void setState(String state) {this.state = state;}
    public void setLatitude(double latitude) {this.latitude = latitude;}
    public void setLongitude(double longitude) {this.longitude = longitude;}
    public void setAverageGHI(double averageGHI) {this.averageGHI = averageGHI;}
    public void setSolarCapacityMWDC(double solarCapacityMWDC) {this.solarCapacityMWDC = solarCapacityMWDC;}
    public void setSolarCapacityMWAC(double solarCapacityMWAC) {this.solarCapacityMWAC = solarCapacityMWAC;}
    //Getters
    public int getEiaID() {return eiaID;}
    public String getProjectName() {return projectName;}
    public String getSolarCOD() {return solarCOD;}
    public String getState() {return state;}
    public double getLatitude() {return latitude;}
    public double getLongitude() {return longitude;}
    public double getAverageGHI() {return averageGHI;}
    public double getSolarCapacityMWDC() {return solarCapacityMWDC;}
    public double getSolarCapacityMWAC() {return solarCapacityMWAC;}

    /*
     * These two methods simply return the length of the string fields so we can write shorter and cleaner code in our
     * main class. There is not one for state becasue all state codes are two characters.
     */
    public int getProjectNameLength() {return this.projectName.length();}
    public int getSolarCODLength() {return this.solarCOD.length();}

    /*
     * This method is required for our implementing of the Comparable interface which will allow us to more easily sort
     * the records as we need to. If the resuling int value is positive then the calling instance is larger, and if
     * negative then the argument instance is larger. If the int value is 0 then they have equal eiaID fields.
     */
    @Override
    public int compareTo(Record record){
        return this.eiaID - record.eiaID;
    }

    /*
     * This method overrides toString to provide an easy terminal way to debug the program once records have been
     * loaded into the program as objects rather than text
     */
    @Override
    public String toString() {
        return  "eiaID=" + eiaID +
                ", projectName='" + projectName + '\'' +
                ", solarCOD='" + solarCOD + '\'' +
                ", state='" + state + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", averageGHI=" + averageGHI +
                ", solarCapacityMWDC=" + solarCapacityMWDC +
                ", solarCapacityMWAC=" + solarCapacityMWAC;
    }
}