package com.mindfulst.pai;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mindfulst.pai.actuators.ActionResult;
import com.mindfulst.pai.actuators.Actuator;
import com.mindfulst.pai.actuators.TimeActuator;
import com.mindfulst.pai.actuators.WeatherActuator;
import com.mindfulst.pai.conversation.Conversation;
import com.mindfulst.pai.conversation.ConversationIntent;
import com.mindfulst.pai.conversation.ConversationState;
import com.mindfulst.pai.conversation.TimeConversation;
import com.mindfulst.pai.conversation.WeatherConversation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ai.wit.sdk.IWitListener;
import ai.wit.sdk.Wit;
import ai.wit.sdk.model.WitOutcome;


public class MainActivity extends ActionBarActivity implements IWitListener {

    Wit witApi;
    TextView sentence;
    TextView response;
    TextView witResults;
    TextView stateResults;
    Gson gson;

    Conversation conversation;
    Actuator[] actuators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        conversation = null;
        actuators = new Actuator[] { new TimeActuator(), new WeatherActuator()};

        final String accessToken = "W2HI75IAJB2Y5RTAPXPEDH4TEPQ6NZ6K";
        witApi = new Wit(accessToken, this);
        witApi.enableContextLocation(getApplicationContext());

        sentence = (TextView) findViewById(R.id.outSentence);
        response = (TextView) findViewById(R.id.outResponse);

        witResults = (TextView) findViewById(R.id.outIntentDebug);
        witResults.setMovementMethod(new ScrollingMovementMethod());

        stateResults = (TextView) findViewById(R.id.outStateDebug);
        stateResults.setMovementMethod(new ScrollingMovementMethod());

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
            if (outcome == null) {
                response.setText("That doesn't make much sense...");
                conversation = null;
                return;
            }

            sentence.setText(outcome.get_text());

            ConversationIntent intent = ConversationIntent.createFrom(outcome);
            if (conversation == null) {
                startConversation(intent);
            } else {
                final boolean canContinue = conversation.update(intent);
                if (!canContinue) {
                    startConversation(intent);
                }
            }

            if (conversation != null) {
                updateStateDebug(conversation.getState());

                if (conversation.hasQuestion()) {
                    response.setText(conversation.nextQuestion());
                } else {
                    ConversationState state = conversation.getState();
                    Actuator action = getAction(state);
                    if (action != null) {
                        ActionResult result = action.actOn(state);
                        if (result != null) {
                            response.setText(result.toString());
                        } else {
                            response.setText("Hum... something's fishy");
                        }
                    } else {
                        response.setText("No action for this intent");
                    }
                    conversation = null;
                }
            } else {
                response.setText("Sorry, I don't know what you mean");
            }
        }
    }

    private WitOutcome getBestOutcome(List<WitOutcome> outcomes) {
        WitOutcome best = null;
        double confidence = 0.5;
        for (WitOutcome o : outcomes) {
            if (o.get_confidence() > confidence) {
                best = o;
            }
        }
        return best;
    }

    private void startConversation(ConversationIntent intent) {
        conversation = null;

        if (intent.type.equals("weather")) {
            conversation = new WeatherConversation();
        } else if (intent.type.equals("time")) {
            conversation = new TimeConversation();
        }

        if (conversation != null) {
            conversation.start(intent);
        }
    }

    private Actuator getAction(ConversationState state) {
        for (Actuator a : actuators) {
            if (a.canActOn(state)) {
                return a;
            }
        }

        return null;
    }

    private void updateStateDebug(ConversationState state) {
        StringBuilder output = new StringBuilder("State:");
        output.append("\nInitial: " + state.getInitialIntent());

        output.append("\nConcepts:");
        for (String concept : state.getConcepts()) {
            String c = String.format("\n\t%s: %s", concept, state.getConceptValue(concept));
            output.append(c);
        }

        output.append("\nLog:");
        for (ConversationIntent intent : state.getLog()) {
            String i = String.format("\n\t(%s, %s)", intent.type, intent.statement);
            output.append(i);
        }

        stateResults.setText(output.toString());
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
