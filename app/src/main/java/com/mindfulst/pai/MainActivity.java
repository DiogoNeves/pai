package com.mindfulst.pai;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rivescript.RiveScript;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    private static final int SPEECH_ACTIVITY_ID = 100;

    private RiveScript scriptEngine;
    private TextView conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scriptEngine = new RiveScript(false);
        scriptEngine.setHandler("perl", new com.rivescript.lang.Perl(
                scriptEngine, "assets/lang/rsp4j.pl"));

        tryLoadScripts();

        conversation = (TextView) findViewById(R.id.outConversation);

        final ImageButton speakButton =
                (ImageButton) findViewById(R.id.speakButton);
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    private void tryLoadScripts() {
        try {
            loadScripts();
        } catch (IOException e) {
            Log.e("conversation", "Failed to load scripts directory");
        }
    }

    private void loadScripts() throws IOException {
        final String directory = "Aiden";

        AssetManager assets = getAssets();
        for (String filename : assets.list(directory)) {
            Log.d("conversation", "Loading " + filename);

            final String path = String.format("%s/%s", directory, filename);
            InputStream scriptStream = assets.open(path);
            loadScript(scriptStream);
            scriptStream.close();
        }
        scriptEngine.sortReplies();
    }

    private void loadScript(InputStream stream) throws IOException {
        InputStreamReader streamReader = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(streamReader);

        StringBuilder content = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            content.append(line);
            content.append("\n");
            line = reader.readLine();
        }

        reader.close();
        streamReader.close();

        scriptEngine.stream(content.toString());
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak");

        try {
            startActivityForResult(intent, SPEECH_ACTIVITY_ID);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SPEECH_ACTIVITY_ID && data != null) {
                final ArrayList<String> speechResult =
                        data.getStringArrayListExtra(
                                RecognizerIntent.EXTRA_RESULTS);
                final String sentence = speechResult.get(0);
                final String reply = scriptEngine.reply("localuser", sentence);

                conversation.append(String.format("you> %s\n", sentence));
                conversation.append(String.format("bot> %s\n", reply));
            }
        }
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
}
