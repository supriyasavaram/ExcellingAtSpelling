package com.bignerdranch.android.excellingatspelling;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by 2018ssavaram on 2/1/2017.
 */
public class FirebaseActivity extends AppCompatActivity {

    private EditText editTextName;
    public static String user;
    private Button buttonSave;

    private static final String HIGH_SCORE="com.bignerdranch.android.excellingatspelling.highscore";

    public static Intent newIntent(Context packageContext, String winner){
        Intent i = new Intent(packageContext, FirebaseActivity.class);
        i.putExtra(HIGH_SCORE, winner);
        return i;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);

        //Firebase.setAndroidContext(this);

        editTextName = (EditText) findViewById(R.id.editTextName);

        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating firebase object
                //Firebase ref = new Firebase(Config.FIREBASE_URL);

                //Getting values to store
                String name = editTextName.getText().toString().trim();
                user = name;

                //Storing values to firebase
                //ref.child("Name").setValue(name);

                //WelcomeActivity.person.setName(user);

                //Intent data = new Intent();
                //data.putExtra(HIGH_SCORE, user);
                //setResult(RESULT_OK, data);
            }
        });
    }
}
