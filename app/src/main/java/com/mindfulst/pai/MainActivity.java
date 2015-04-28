package com.mindfulst.pai;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;

import ai.wit.sdk.IWitListener;
import ai.wit.sdk.Wit;
import ai.wit.sdk.model.WitOutcome;


public class MainActivity extends ActionBarActivity implements IWitListener {

    Wit witApi;
    TextView sentence;
    TextView witResults;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String accessToken = "W2HI75IAJB2Y5RTAPXPEDH4TEPQ6NZ6K";
        witApi = new Wit(accessToken, this);
        witApi.enableContextLocation(getApplicationContext());

        sentence = (TextView) findViewById(R.id.outSentence);

        witResults = (TextView) findViewById(R.id.outIntentDebug);
        witResults.setMovementMethod(new ScrollingMovementMethod());

        gson = new GsonBuilder().setPrettyPrinting().create();

        TextView whatInput = (TextView) findViewById(R.id.inWhat);
        final NotificationManager notifier =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final int notificationId = 1;
        whatInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Notification simpleNotification = new NotificationCompat.Builder(v.getContext())
                        .setSmallIcon(android.R.drawable.sym_def_app_icon)
                        .setContentTitle("Testing")
                        .setContentText(v.getText())
                        .build();
                notifier.notify(notificationId, simpleNotification);
                return false;
            }
        });
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

    public void toggleListening(View v) {
        try {
            witApi.toggleListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void witDidGraspIntent(ArrayList<WitOutcome> witOutcomes, String s, Error error) {
        if (error != null) {
            witResults.setText(error.getLocalizedMessage());
        } else {
            String output = gson.toJson(witOutcomes);
            witResults.setText(output);
            sentence.setText(witOutcomes.get(0).get_text());
        }
    }

    @Override
    public void witDidStartListening() {
        sentence.setText("Witting");
    }

    @Override
    public void witDidStopListening() {
        sentence.setText("Processing");
    }

    @Override
    public void witActivityDetectorStarted() {
        sentence.setText("Listening");
    }

    @Override
    public String witGenerateMessageId() {
        return null;
    }
}
