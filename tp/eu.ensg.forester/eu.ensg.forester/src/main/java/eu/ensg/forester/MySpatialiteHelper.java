package eu.ensg.forester;

import android.content.Context;
import android.location.Location;

import java.io.IOException;

import eu.ensg.spatialite.SpatialiteOpenHelper;
import eu.ensg.spatialite.geom.XY;
import jsqlite.Database;

/**
 * Created by cyann on 20/12/15.
 */
public class MySpatialiteHelper extends SpatialiteOpenHelper {

    public static final String DATABASE_NAME = "Spatial.sqlite";
    public static final int DATABASE_VERSION = 2;

    public static final int GPS_SRID = 4326;

    //public static final String TABLE_FORESTER = "Forester";
    public static final String TABLE_INTEREST = "PointOfInterest";
    public static final String TABLE_SECTOR = "Sector";

    public static final String COLUMN_PK_ID = "id";
    public static final String COLUMN_FK_FORESTER_ID = "foresterId";
    public static final String COLUMN_MASTER_DB_ID = "masterDBid";
    public static final String COLUMN_NAME = "name";
    //public static final String COLUMN_FIRSTNAME = "firstName";
    //public static final String COLUMN_LASTNAME = "lastName";
    //public static final String COLUMN_SERIAL = "serial";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_COORDINATE = "coordinate";

    //public static final String FK_INTEREST_FORESTER = "FK_" + TABLE_INTEREST + "_" + TABLE_FORESTER;
    //public static final String FK_SECTOR_FORESTER = "FK_" + TABLE_SECTOR + "_" + TABLE_FORESTER;

    public static final String IDX_INTEREST_FORESTER = "IDX_" + TABLE_INTEREST + "_" + COLUMN_FK_FORESTER_ID;
    public static final String IDX_SECTOR_FORESTER = "IDX_" + TABLE_SECTOR + "_" + COLUMN_FK_FORESTER_ID;

    /*public static final String CREATE_FORESTER =
            "create table " + TABLE_FORESTER +
                    "(" + COLUMN_PK_ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FIRSTNAME + " string NOT NULL, " +
                    COLUMN_LASTNAME + " string NOT NULL, " +
                    COLUMN_SERIAL + " string NOT NULL" + ");";*/

    // TODO: ATTENTION !!!! MasterDBID pour synchroniser avec la master DB
    public static final String CREATE_INTEREST =
            "create table " + TABLE_INTEREST +
                    "(" + COLUMN_PK_ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                    //COLUMN_FK_FORESTER_ID + " integer NOT NULL," +
                    COLUMN_NAME + " string NOT NULL, " +
                    COLUMN_DESCRIPTION + " string, " +
                    COLUMN_MASTER_DB_ID + " integer " +
                    /*", CONSTRAINT " + FK_INTEREST_FORESTER +
                    "FOREIGN KEY (" + COLUMN_FK_FORESTER_ID + ") " +
                    "REFERENCE " + TABLE_FORESTER + "(" + COLUMN_PK_ID + ")" +*/
                    ");";

    /*public static final String CREATE_INTEREST_INDEX =
            "CREATE INDEX " + IDX_INTEREST_FORESTER + " ON " + TABLE_INTEREST + "(" + COLUMN_FK_FORESTER_ID + ")";*/

    public static final String CREATE_INTEREST_COORDINATE =
            "SELECT AddGeometryColumn('" + TABLE_INTEREST + "', '" + COLUMN_COORDINATE + "', " + GPS_SRID + ", 'POINT', 'XY', 0);";

    public static final String CREATE_SECTOR =
            "create table " + TABLE_SECTOR +
                    "(" + COLUMN_PK_ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                    //COLUMN_FK_FORESTER_ID + " integer NOT NULL," +
                    COLUMN_NAME + " string NOT NULL, " +
                    COLUMN_DESCRIPTION + " string, " +
                    COLUMN_MASTER_DB_ID + " integer" +
                    /*", CONSTRAINT " + FK_SECTOR_FORESTER +
                    "FOREIGN KEY (" + COLUMN_FK_FORESTER_ID + ") " +
                    "REFERENCE " + TABLE_FORESTER + "(" + COLUMN_PK_ID + ")" +*/
                    ");";

    /*public static final String CREATE_SECTOR_INDEX =
            "CREATE INDEX " + IDX_SECTOR_FORESTER + " ON " + TABLE_SECTOR + "(" + COLUMN_FK_FORESTER_ID + ")";*/

    public static final String CREATE_SECTOR_COORDINATE =
            "SELECT AddGeometryColumn('" + TABLE_SECTOR + "', '" + COLUMN_COORDINATE + "', " + GPS_SRID + ", 'POLYGON', 'XY', 0);";

    public MySpatialiteHelper(Context context) throws jsqlite.Exception, IOException {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    public static XY coordFactory(Location location) {
        return new XY(location.getLongitude(), location.getLatitude());
    }

    @Override
    public void onCreate(Database db) throws jsqlite.Exception {
        //super.exec(CREATE_FORESTER);
        super.exec(CREATE_INTEREST);
        super.exec(CREATE_INTEREST_COORDINATE);
        //super.exec(CREATE_INTEREST_INDEX);
        super.exec(CREATE_SECTOR);
        super.exec(CREATE_SECTOR_COORDINATE);
        //super.exec(CREATE_SECTOR_INDEX);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) throws jsqlite.Exception {

        switch (oldVersion) {

            case 1:
                // mise à jour de 1 -> 2
                super.exec(CREATE_SECTOR);
                super.exec(CREATE_SECTOR_COORDINATE);
                break;
            default:
                throw new IllegalStateException(
                        "onUpgrade() with unknown oldVersion" + oldVersion + " to " + newVersion);
        }

    }
}
