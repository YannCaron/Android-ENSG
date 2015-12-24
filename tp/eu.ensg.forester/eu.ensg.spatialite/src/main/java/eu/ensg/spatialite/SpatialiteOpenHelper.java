package eu.ensg.spatialite;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import jsqlite.Callback;
import jsqlite.Database;
import jsqlite.Stmt;

/**
 * Created by cyann on 20/12/15.
 */
public abstract class SpatialiteOpenHelper {

    // constant
    static final String PREFERENCE_NAME = "Spatialite";
    static final String KEY_VERSION = "version";

    static final String INIT_SPATIAL_METADATA = "SELECT InitSpatialMetaData();";

    // attribure
    private final Database database;

    // constructor

    /**
     * Default constructor
     * @param version the database version
     * @throws jsqlite.Exception spatialite exception
     * @throws IOException file exception
     */
    public SpatialiteOpenHelper(int version) throws jsqlite.Exception, IOException {

        // spatialite file
        File spatialDbFile = getDatabasePath();

        if (!spatialDbFile.getParentFile().exists()) {
            File dirDb = spatialDbFile.getParentFile();
            Log.i(this.getClass().getSimpleName(), "making directory: " + spatialDbFile.getParentFile());
            if (!dirDb.mkdir()) {
                throw new IOException(this.getClass().getSimpleName() + "Could not create dirDb: " + dirDb.getAbsolutePath());
            }
        }

        Log.w(this.getClass().getName(), "Spatialite database created on [" + spatialDbFile.getAbsolutePath() + "]");
        database = new Database();
        database.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE | jsqlite.Constants.SQLITE_OPEN_CREATE);

        int oldVersion = getPersistedVersion();

        if (oldVersion == -1) {
            // TODO: !!!! Initialiser les metadata spacial, sinon ne fonctionne pas.
            exec(INIT_SPATIAL_METADATA);
            // TODO: !!!! basé sur le pattern "Template method" du GoF
            onCreate(database);
            Log.w(this.getClass().getName(), "Spatialite first installation, database created.");
        } else if (oldVersion != version) {
            Log.w(this.getClass().getName(), "UPDATE DATABASE");
            // TODO: !!!! basé sur le pattern "Template method" du GoF
            onUpgrade(database, oldVersion, version);
            Log.w(this.getClass().getName(), "Spatialite update installation from version [" + oldVersion + "] to [ " + version + " ].");
        }

        // !!!! Sauve la version dans un conteneur persisté pour un prochain lancement
        setPersistedVersion(version);
    }

    // property

    /**
     * The database accessor
     * @return the database object
     */
    public Database getDatabase() {
        return database;
    }

    // method

    /**
     * Dump the query result to string
     * @param query sql query
     * @return the data dump to string
     * @throws jsqlite.Exception spatialite exception
     */
    public String dumpQuery(String query) throws jsqlite.Exception {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Execute query: ").append(query).append("\n\n");

        Stmt stmt = database.prepare(query);

        //in my example, num columns is 9
        //I don't know if minus 1 is always needed here
        int maxColumns = stmt.column_count();

        for (int i = 0; i < maxColumns; i++) {
            if (i != 0) stringBuilder.append(" | ");
            stringBuilder.append(stmt.column_name(i));
        }
        stringBuilder.append("\n--------------------------------------------\n");

        int rowIndex = 0;
        while (stmt.step()) {
            for (int i = 0; i < maxColumns; i++) {
                if (i != 0) stringBuilder.append(" | ");
                stringBuilder.append(stmt.column_string(i));
            }
            stringBuilder.append("\n");

            if (rowIndex++ > 100) break;
        }
        stringBuilder.append("\n--------------------------------------------\n");
        stmt.close();

        stringBuilder.append("\ndump done\n");

        return stringBuilder.toString();
    }

    /**
     * Execute a sql query
     * @param sql the sql query
     * @throws jsqlite.Exception spatialite exception
     */
    public void exec(String sql) throws jsqlite.Exception {
        Log.i(this.getClass().getName(), "Execute query: " + sql);
        database.exec(sql, new Callback() {
            @Override
            public void columns(String[] coldata) {
            }

            @Override
            public void types(String[] types) {
            }

            @Override
            public boolean newrow(String[] rowdata) {
                return false;
            }
        });
        Log.i(this.getClass().getName(), "Query executed successfully !");
    }

    /**
     * Close the database
     * @throws jsqlite.Exception spatialite exception
     */
    public void close() throws jsqlite.Exception {
        database.close();
    }

    // abstract method

    /**
     * Should return the path where database file is stored
     * @return the database file
     */
    protected abstract File getDatabasePath();

    /**
     * Should return the persisted version (for version comparison mechanism)
     * @return the persisted version
     */
    protected abstract int getPersistedVersion();

    /**
     * Should store the persisted version (for version comparison mechanism)
     * @param version
     */
    protected abstract void setPersistedVersion(int version);

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    public abstract void onCreate(Database db) throws jsqlite.Exception;

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
    public abstract void onUpgrade(Database db, int oldVersion, int newVersion) throws jsqlite.Exception;


}
