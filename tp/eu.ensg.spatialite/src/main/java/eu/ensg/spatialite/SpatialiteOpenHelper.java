package eu.ensg.spatialite;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import jsqlite.Database;

/**
 * Created by cyann on 20/12/15.
 */
public abstract class SpatialiteOpenHelper {

    public static final String PREFERENCE_NAME = "Spatialite";
    public static final String KEY_VERSION = "version";

    public static final String INIT_SPATIAL_METADATA = "SELECT InitSpatialMetaData();";
    protected final String name;
    private final Context context;
    private final SpatialiteDatabase database;

    public SpatialiteOpenHelper(Context context, String name, int version) throws jsqlite.Exception, IOException {
        this.context = context;
        this.name = name;

        // spatialite file
        // TODO: Le path de la base de donnée [/data/data/[app_name]/databases/[name]]
        File spatialDbFile = getDatabaseFile();

        if (!spatialDbFile.getParentFile().exists()) {
            File dirDb = spatialDbFile.getParentFile();
            Log.i(this.getClass().getSimpleName(), "making directory: " + spatialDbFile.getParentFile());
            if (!dirDb.mkdir()) {
                throw new IOException(this.getClass().getSimpleName() + "Could not create dirDb: " + dirDb.getAbsolutePath());
            }
        }

        Log.w(this.getClass().getName(), "Spatialite database created on [" + spatialDbFile.getAbsolutePath() + "]");
        database = new SpatialiteDatabase();
        database.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE | jsqlite.Constants.SQLITE_OPEN_CREATE);

        int oldVersion = getCurrentVersion();

        if (oldVersion == -1) {
            // !!!! Initialiser les metadata spacial, sinon ne fonctionne pas.
            database.exec(INIT_SPATIAL_METADATA);

            // !!!! basé sur le pattern "Template method" du GoF
            onCreate(database);
            Log.w(this.getClass().getName(), "Spatialite first installation, database created.");
        } else if (oldVersion != version) {
            Log.w(this.getClass().getName(), "UPDATE DATABASE");
            // !!!! basé sur le pattern "Template method" du GoF
            onUpgrade(database, oldVersion, version);
            Log.w(this.getClass().getName(), "Spatialite update installation from version [" + oldVersion + "] to [ " + version + " ].");
        }

        // !!!! Sauve la version dans un conteneur persisté pour un prochain lancement
        updateVersion(version);
    }

    public int getCurrentVersion() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(KEY_VERSION, -1);
    }

    public void updateVersion(int version) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_VERSION, version);
        editor.commit();
        editor.apply();
    }

    public final String getDatabaseName() {
        return name;
    }

    public final File getDatabaseFile() {
        return context.getDatabasePath(name);
    }

    public SpatialiteDatabase getDatabase() {
        return database;
    }

    public void close() throws jsqlite.Exception {
        database.close();
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    public abstract void onCreate(SpatialiteDatabase db) throws jsqlite.Exception;

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    public abstract void onUpgrade(SpatialiteDatabase db, int oldVersion, int newVersion) throws jsqlite.Exception;


}
