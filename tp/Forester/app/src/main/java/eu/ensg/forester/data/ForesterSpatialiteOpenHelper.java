package eu.ensg.forester.data;

import android.content.Context;

import java.io.IOException;

import eu.ensg.spatialite.SpatialiteOpenHelper;
import jsqlite.Database;
import jsqlite.Exception;

/**
 * Created by cyann on 06/02/16.
 */
public class ForesterSpatialiteOpenHelper extends SpatialiteOpenHelper {

    public static final String DB_FILE_NAME = "Spatialite.sqlite";
    public static final int VERSION = 1;
    public static final int GPS_SRID = 4326;

    public ForesterSpatialiteOpenHelper(Context context) throws Exception, IOException {
        super(context, DB_FILE_NAME, VERSION);
    }

    @Override
    public void onCreate(Database db) throws jsqlite.Exception {
        // table forester
        getDatabase().exec("CREATE TABLE Forester (\n" +
                "ID integer PRIMARY KEY AUTOINCREMENT, \n" +
                "FirstName string NOT NULL, \n" +
                "LastName string NOT NULL, \n" +
                "Serial string NO NULL\n" +
                ");");

        // table point of interest
        getDatabase().exec("CREATE TABLE PointOfInterest (\n" +
                "ID integer PRIMARY KEY AUTOINCREMENT, \n" +
                "ForesterID integer NOT NULL,\n" +
                "Name string NOT NULL, \n" +
                "Description string,\n" +
                "CONSTRAINT FK_poi_forester\n" +
                "   FOREIGN KEY (foresterID)\n" +
                "   REFERENCES forester (id)\n" +
                ");");

        getDatabase().exec("SELECT AddGeometryColumn('PointOfInterest', 'Position', " + GPS_SRID + ", 'POINT', 'XY', 1);");

        // table district
        getDatabase().exec("CREATE TABLE District (\n" +
                "ID integer PRIMARY KEY AUTOINCREMENT, \n" +
                "ForesterID integer NOT NULL,\n" +
                "Name string NOT NULL, \n" +
                "Description string,\n" +
                "CONSTRAINT FK_district_forester\n" +
                "   FOREIGN KEY (foresterID)\n" +
                "   REFERENCES forester (id)\n" +
                ");");

        getDatabase().exec("SELECT AddGeometryColumn('District', 'Area', " + GPS_SRID + ", 'POLYGON', 'XY', 1);");
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) throws Exception {

    }
}
