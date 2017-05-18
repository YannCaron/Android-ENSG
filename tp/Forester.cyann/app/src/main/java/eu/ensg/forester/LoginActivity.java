package eu.ensg.forester;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import eu.ensg.forester.data.ForesterSpatialiteOpenHelper;
import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.SpatialiteOpenHelper;
import jsqlite.Stmt;

public class LoginActivity extends AppCompatActivity implements Constants {

    // les vues
    private EditText editSerial;
    private Button buttonLogin;
    private Button buttonCreate;

    // les préférences
    private SharedPreferences preferences;

    // database
    private SpatialiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Récupère les instances des vues
        editSerial = (EditText) findViewById(R.id.serial);
        buttonLogin = (Button) findViewById(R.id.login);
        buttonCreate = (Button) findViewById(R.id.create);

        // Récupère les préférences
        preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        // gére si l'extra a été passé en paramètre
        String s = getIntent().getStringExtra(EXTRA_SERIAL);

        if (s != null) {
            editSerial.setText(s);
        } else {
            editSerial.setText(preferences.getString(PREFERENCE_SERIAL, ""));
        }

        // gère les événements
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_onClick(v);
            }
        });

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_onClick(v);
            }
        });

        // database
        initDatabase();

    }

    private void login_onClick(View view) {
        String serial = editSerial.getText().toString();

        // requête sql
        try {
            Stmt stmt = database.prepare("SELECT * FROM Forester where Serial = " + DatabaseUtils.sqlEscapeString(serial));

            if (stmt.step()) {
                int foresterID = stmt.column_int(0);

                // stoque le serial dans la mémoire de l'appareil
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREFERENCE_SERIAL, serial);
                editor.commit();
                editor.apply();

                // appel la map
                Intent intent = new Intent(this, MapsActivity.class);
                intent.putExtra(EXTRA_FORESTER_ID, foresterID);
                startActivity(intent);

            } else {
                Toast.makeText(this, "User does not exists, please create it !", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, CreateUserActivity.class);
                startActivity(intent);
            }

        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }


    }

    private void create_onClick(View view) {
        Intent intent = new Intent(this, CreateUserActivity.class);
        startActivity(intent);
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
