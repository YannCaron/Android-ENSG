package eu.ensg.forester;

import android.content.Intent;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import eu.ensg.forester.data.ForesterSpatialiteOpenHelper;
import eu.ensg.spatialite.SpatialiteDatabase;

public class CreateUserActivity extends AppCompatActivity implements Constants {

    // les vues
    private EditText editFirstName;
    private EditText editLastName;
    private EditText editSerial;
    private Button buttonCreate;

    // database
    private SpatialiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        // Récupère les instances des vues
        editFirstName = (EditText)findViewById(R.id.first_name);
        editLastName = (EditText)findViewById(R.id.last_name);
        editSerial = (EditText)findViewById(R.id.serial);
        buttonCreate = (Button)findViewById(R.id.create);

        // gère les événements
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_onClick(v);
            }
        });

        // database
        initDatabase();

    }

    private void create_onClick(View view) {

        try {
            database.exec("INSERT INTO Forester (FirstName, LastName, Serial) " +
                    "VALUES (" + DatabaseUtils.sqlEscapeString(editFirstName.getText().toString()) + ", " +
                    DatabaseUtils.sqlEscapeString(editLastName.getText().toString()) + ", " +
                    DatabaseUtils.sqlEscapeString(editSerial.getText().toString()) + ")");

            // Appel l'activity login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(EXTRA_SERIAL, editSerial.getText().toString());
            startActivity(intent);

        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }


    }

    private void initDatabase() {

        try {
            database = new ForesterSpatialiteOpenHelper(this).getDatabase();
        } catch (jsqlite.Exception | IOException e) {
            e.printStackTrace();
        }

    }
}
