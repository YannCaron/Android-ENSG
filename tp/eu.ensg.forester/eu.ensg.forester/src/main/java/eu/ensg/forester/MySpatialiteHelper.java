package eu.ensg.forester;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.io.IOException;

import eu.ensg.spatialite.geom.XY;
import jsqlite.*;

/**
 * Created by cyann on 20/12/15.
 */
public class MySpatialiteHelper extends SpatialiteOpenHelper {

    public static final String DATABASE_NAME = "Spatial.sqlite";
    public static final int DATABASE_VERSION = 1;

    public static final int GPS_SRID = 4326;

    public static final String TABLE_INTEREST = "PointOfInterest";
    public static final String TABLE_SECTOR = "Sector";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_COORDINATE = "coordinate";

    public static final String CREATE_INTEREST =
            "create table " + TABLE_INTEREST +
                    "(" + COLUMN_ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " string NOT NULL, " +
                    COLUMN_COMMENT + " string" + ");";

    public static final String CREATE_COLUMN_COORDINATE =
            "SELECT AddGeometryColumn('" + TABLE_INTEREST + "', '" + COLUMN_COORDINATE + "', " + GPS_SRID + ", 'POINT', 'XY', 0);";

    public MySpatialiteHelper(Context context) throws jsqlite.Exception, IOException {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    public static XY coordFactory(Location location) {
        return new XY(location.getLongitude(), location.getLatitude());
    }

    @Override
    public void onCreate(Database db) throws jsqlite.Exception {
        super.exec(CREATE_INTEREST);
        super.exec(CREATE_COLUMN_COORDINATE);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) throws jsqlite.Exception {
        Log.w(MySpatialiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        db.complete("DROP TABLE IF EXISTS " + TABLE_INTEREST);
        onCreate(db);
    }
}
