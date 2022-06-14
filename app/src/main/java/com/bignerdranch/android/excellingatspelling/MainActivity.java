package com.bignerdranch.android.excellingatspelling;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Button mNextButton;
    private Button mSubmitButton;
    private Button mAudioButton;
    private TextView mTitle;
    private EditText editTextName;
    private String answer;
    private TextToSpeech mTextToSpeech;
    private TextToSpeech mQuestion;
    private ImageView mImageView;

    private MediaPlayer c;
    private MediaPlayer i;
    private MediaPlayer w;
    private MediaPlayer o;

    private int mCurrentIndex = 0;
    private ArrayList<String> mQuestionBank;

    private Firebase ref;
    private int tries = 3;
    private Winner person;
    private int high;
    private int score = 0;

    private TextView mScoreTextView;
    private TextView mHighScoreTextView;
    private TextView mTriesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        mTriesTextView = (TextView)findViewById(R.id.tries_text_view);
        mTriesTextView.setText("Lives Left: " + tries);
        mScoreTextView = (TextView)findViewById(R.id.score_text_view);
        mScoreTextView.setText("Score: " + score + " / 26");
        mHighScoreTextView = (TextView)findViewById(R.id.high_text_view);

        person = new Winner();
        Firebase.setAndroidContext(this);
        ref = new Firebase(Config.FIREBASE_URL);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //Getting the data from snapshot
                    person = postSnapshot.getValue(Winner.class);

                    high = person.getScore();

                    //Adding it to a string
                    String string = "High Score: " +  high + " / 26";

                    //Displaying it on textview
                    mHighScoreTextView.setText(string);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        c = MediaPlayer.create(getApplicationContext(), R.raw.correct);
        i = MediaPlayer.create(getApplicationContext(), R.raw.incorrect);
        w = MediaPlayer.create(getApplicationContext(), R.raw.win);
        o = MediaPlayer.create(getApplicationContext(), R.raw.gameover);

        Context context = getApplicationContext();
        mQuestionBank = new ArrayList<String>();
        try {
            mQuestionBank = input(context, R.raw.words);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mTitle = (TextView)findViewById(R.id.title);
        mTitle.setText(R.string.sTitle);

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextName.setEnabled(true);


        mQuestion = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mQuestion.setLanguage(Locale.US);
                }
            }
        });

        mSubmitButton = (Button) findViewById(R.id.submit_button);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer = editTextName.getText().toString().trim();
                checkAnswer(answer);
            }
        });
        mSubmitButton.setEnabled(true);

        mNextButton=(Button)findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                updateQuestion();
            }
        });
        mNextButton.setEnabled(false);

        mAudioButton=(Button)findViewById(R.id.audio_button);
        mAudioButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String toSpeak = mQuestionBank.get(mCurrentIndex);
                mQuestion.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        mAudioButton.setEnabled(true);

        mTextToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        mImageView = (ImageView) findViewById(R.id.picture);
        //mImageView.setImageDrawable(null);
    }

    private void checkAnswer(String pressed){
        String answer = mQuestionBank.get(mCurrentIndex);
        int messageResId = 0;

        String toSpeak = pressed;
        mTextToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

        if (pressed.equalsIgnoreCase(answer)) {
            //mCurrentIndex = (mCurrentIndex + 1)% mQuestionBank.size();
            int temp = (mCurrentIndex + 1)% mQuestionBank.size();
            score++;
            mScoreTextView.setText("Score: " + score + " / 26");
            messageResId = R.string.positive;
            if(temp == 0) {
                if (score > high) {
                    high = score;
                    person.setScore(high);
                    ref.child("Winner").setValue(person);
                    mHighScoreTextView.setText("High Score: " + high + " / 26");
                    Toast.makeText(this, "New High Score!", Toast.LENGTH_SHORT).show();
                }
                w.start();
                mImageView.setImageResource(R.drawable.win_meme);
                mSubmitButton.setEnabled(false);
                mNextButton.setEnabled(false);
                editTextName.setEnabled(false);
            }
            else {
                c.start();
                mImageView.setImageResource(R.drawable.good_meme);
                mSubmitButton.setEnabled(false);
                mNextButton.setEnabled(true);
                editTextName.setEnabled(false);
            }
        } else{
            tries--;
            mTriesTextView.setText("Lives Left: " + tries);
            messageResId = R.string.negative;
            if(tries>0){
                i.start();
                mImageView.setImageResource(R.drawable.bad_meme);
            }
            else{
                o.start();
                mImageView.setImageResource(R.drawable.gameover_meme);
                mSubmitButton.setEnabled(false);
                mNextButton.setEnabled(false);
                editTextName.setEnabled(false);
                if (score > high) {
                    high = score;
                    person.setScore(high);
                    ref.child("Winner").setValue(person);
                    mHighScoreTextView.setText("High Score: " + high + " / 26");
                    Toast.makeText(this, "New High Score!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }

    private void updateQuestion(){
        mSubmitButton.setEnabled(true);
        mNextButton.setEnabled(false);
        editTextName.setEnabled(true);
        editTextName.getText().clear();
        mImageView.setImageDrawable(null);
        mCurrentIndex = (mCurrentIndex + 1)% mQuestionBank.size();
        /*if(mCurrentIndex == 0) {
            if (score > high) {
                high = score;
                person.setScore(high);
                ref.child("Winner").setValue(person);
                Toast.makeText(this, "New High Score!", Toast.LENGTH_SHORT).show();
            }
            mImageView.setImageResource(R.drawable.win_meme);
            mSubmitButton.setEnabled(false);
            mNextButton.setEnabled(false);
            editTextName.setEnabled(false);
        }*/
        //mTriesTextView.setText("Lives Left: " + tries);
        //mScoreTextView.setText("Score: " + score + " / 26");
        //mHighScoreTextView.setText("High Score: " + high + " / 26");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onPause(){
        if(mTextToSpeech !=null){
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
        super.onPause();
    }

    public static ArrayList<String> input(Context ctx, int filename) throws Exception
    {
        InputStream buildinginfo = ctx.getResources().openRawResource(filename);

        InputStreamReader inputreader = new InputStreamReader(buildinginfo);
        BufferedReader buffreader = new BufferedReader(inputreader);

        String line;
        ArrayList<String> temp = new ArrayList<String>();

        try{
            while((line = buffreader.readLine()) != null){
                temp.add(line);
            }
        }catch (IOException e){
            return null;
        }
        return temp;
    }
}
