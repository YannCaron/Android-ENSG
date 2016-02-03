package eu.ensg.forester;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity implements Constants {

    // les vues
    private EditText editSerial;
    private Button buttonLogin;
    private Button buttonCreate;

    // les préférences
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Récupère les instances des vues
        editSerial = (EditText)findViewById(R.id.serial);
        buttonLogin = (Button)findViewById(R.id.login);
        buttonCreate = (Button)findViewById(R.id.create);

        // Récupère les préférences
        preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        // gére si l'extra a été passé en paramètre
        Intent intent = getIntent();
        String s = intent.getStringExtra(EXTRA_SERIAL);
        Log.w(this.getClass().getName(), "EXTRA " + s);

        if (!"".equals(s)) {
            editSerial.setText(s);
        } else {
            editSerial.setText(preferences.getString(KEY_SERIAL, ""));
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
    }

    private void login_onClick(View view) {
        String serial = editSerial.getText().toString();

        // stoque le serial dans la mémoire de l'appareil
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_SERIAL, serial);
        editor.commit();
        editor.apply();

    }

    private void create_onClick(View view) {
        Intent intent = new Intent(this, CreateUserActivity.class);
        startActivity(intent);
    }

}
