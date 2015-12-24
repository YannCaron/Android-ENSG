package eu.ensg.forester;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import eu.ensg.spatialite.SpatialiteOpenHelper;
import eu.ensg.spatialite.geom.XY;
import jsqlite.Database;

/**
 * Created by cyann on 20/12/15.
 */
public class MySpatialiteHelper extends SpatialiteOpenHelper {

    // constant
    static final String DATABASE_NAME = "Spatial.sqlite";
    static final int DATABASE_VERSION = 2;

    static final int GPS_SRID = 4326;

    static final String TABLE_INTEREST = "PointOfInterest";
    static final String TABLE_SECTOR = "Sector";

    static final String COLUMN_ID = "id";
    static final String COLUMN_NAME = "name";
    static final String COLUMN_COMMENT = "comment";
    static final String COLUMN_COORDINATE = "coordinate";

    static final String CREATE_INTEREST =
            "create table " + TABLE_INTEREST +
                    "(" + COLUMN_ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " string NOT NULL, " +
                    COLUMN_COMMENT + " string" + ");";

    static final String CREATE_INTEREST_COORDINATE =
            "SELECT AddGeometryColumn('" + TABLE_INTEREST + "', '" + COLUMN_COORDINATE + "', " + GPS_SRID + ", 'POINT', 'XY', 0);";

    static final String CREATE_SECTOR =
            "create table " + TABLE_SECTOR +
                    "(" + COLUMN_ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " string NOT NULL, " +
                    COLUMN_COMMENT + " string" + ");";

    static final String CREATE_SECTOR_COORDINATE =
            "SELECT AddGeometryColumn('" + TABLE_SECTOR + "', '" + COLUMN_COORDINATE + "', " + GPS_SRID + ", 'POLYGON', 'XY', 0);";

    // attribure
    private final Context context;

    // constructor and factory

    /**
     * Default constructor
     * @param context Android context
     * @throws jsqlite.Exception sqlite exceptions
     * @throws IOException file exception
     */
    public MySpatialiteHelper(Context context) throws jsqlite.Exception, IOException {
        super(DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Create a new XY coordinate from android location
     * @param location the android location
     * @return an XY spatialite coordinate
     */
    public static XY coordFactory(Location location) {
        return new XY(location.getLongitude(), location.getLatitude());
    }

    // method implements

    /** {@inheritDoc} */
    @Override
    protected File getDatabasePath() {
        // TODO: Le path de la base de donnée [/data/data/[app_name]/databases/[name]]
        return context.getDatabasePath(DATABASE_NAME);
    }

    /** {@inheritDoc} */
    @Override
    protected int getPersistedVersion() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(KEY_VERSION, -1);
    }

    /** {@inheritDoc} */
    @Override
    protected void setPersistedVersion(int version) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_VERSION, version);
        editor.commit();
        editor.apply();
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(Database db) throws jsqlite.Exception {
        super.exec(CREATE_INTEREST);
        super.exec(CREATE_INTEREST_COORDINATE);
        super.exec(CREATE_SECTOR);
        super.exec(CREATE_SECTOR_COORDINATE);
    }

    /** {@inheritDoc} */
    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) throws jsqlite.Exception {
        Log.w(MySpatialiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        int delta = newVersion - oldVersion;

        if (newVersion == 2) {
            if (delta <= 1) {
                super.exec(CREATE_SECTOR);
                super.exec(CREATE_SECTOR_COORDINATE);

            }
        }
    }
}
