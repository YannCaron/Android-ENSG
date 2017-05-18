package eu.ensg.forester;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.ensg.forester.data.ForesterSpatialiteOpenHelper;
import eu.ensg.forester.data.ListAdapter;
import eu.ensg.spatialite.SpatialiteOpenHelper;
import jsqlite.Database;
import jsqlite.Stmt;

public class DataListActivity extends AppCompatActivity implements Constants {

    // view
    GridView gridview;

    // attributs
    String sqlQuery = null;
    Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_list);

        initDatabase();

        // Récupère l'extra passé en paramètre
        sqlQuery = getIntent().getStringExtra(EXTRA_SQL);

        // récupère les vues
        gridview = (GridView) findViewById(R.id.gridview);

        // lit les données
        try {
            List<String> data = new ArrayList<>();
            Stmt stmt = database.prepare(sqlQuery);
            int columnCount = stmt.column_count();

            while (stmt.step()) {
                data.add(String.valueOf(stmt.column_int(0)));
                data.add(stmt.column_string(1));
                data.add(stmt.column_string(2));
                data.add(stmt.column_string(3));
            }

            gridview.setNumColumns(columnCount);
            gridview.setAdapter(new ListAdapter(this, data, columnCount));

        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }

    }

    private void initDatabase() {

        try {
            SpatialiteOpenHelper helper = new ForesterSpatialiteOpenHelper(this);
            database = helper.getDatabase();
        } catch (jsqlite.Exception | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot initialize database !", Toast.LENGTH_LONG).show();
            System.exit(0);
        }

    }
}
