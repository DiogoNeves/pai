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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.wit.sdk.IWitListener;
import ai.wit.sdk.Wit;
import ai.wit.sdk.model.WitOutcome;


public class MainActivity extends ActionBarActivity implements IWitListener {

    Wit witApi;
    TextView sentence;
    TextView inference;
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
        inference = (TextView) findViewById(R.id.outInference);

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

            WitOutcome outcome = getBestOutcome(witOutcomes);
            sentence.setText(outcome.get_text());
            inferAction(outcome);
        }
    }

    private WitOutcome getBestOutcome(List<WitOutcome> outcomes) {
        WitOutcome best = null;
        double confidence = 0.0;
        for (WitOutcome o : outcomes) {
            if (o.get_confidence() > confidence) {
                best = o;
            }
        }
        return best;
    }

    private void inferAction(WitOutcome outcome) {
        // This is where it should use proper inference to decide what to do
        if (outcome.get_intent().equals("UNKNOWN")) {
            inference.setText("I don't know what you mean");
        } else {
            StringBuilder result = new StringBuilder();
            result.append("You want to know the ");
            result.append(outcome.get_intent());

            Map<String, JsonElement> entities = outcome.get_entities();
            if (entities.containsKey("location")) {
                JsonArray locations = entities.get("location").getAsJsonArray();
                JsonElement locationElement = locations.get(0);

                result.append(" in ");
                result.append(locationElement.getAsJsonObject().get("value").getAsString());
            }

            inference.setText(result.toString());
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
