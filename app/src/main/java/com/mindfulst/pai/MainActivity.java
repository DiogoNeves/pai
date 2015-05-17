package com.mindfulst.pai;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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


public class MainActivity extends AppCompatActivity {

    private static final int SPEECH_ACTIVITY_ID = 100;
    private static final String UTTERANCE_ID = "speaker";

    private RiveScript scriptEngine;
    private TextToSpeech speaker;

    private TextView conversation;
    private EditText userSentenceInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scriptEngine = new RiveScript(true);
        scriptEngine.setHandler("perl", new com.rivescript.lang.Perl(
                scriptEngine, "assets/lang/rsp4j.pl"));
        tryLoadScripts();

        speaker = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            speaker.setLanguage(Locale.UK);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "TextToSpeech failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        conversation = (TextView) findViewById(R.id.outConversation);
        userSentenceInput = (EditText) findViewById(R.id.userSentenceBox);

        userSentenceInput.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        final boolean isSend = actionId == EditorInfo.IME_ACTION_SEND;
                        final boolean isReturn =
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
                        if (isSend || isReturn) {
                            processUserSentence();
                            userSentenceInput.getText().clear();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

        final Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processUserSentence();
            }
        });

        final ImageButton speakButton =
                (ImageButton) findViewById(R.id.speakButton);
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (speaker != null) {
            speaker.stop();
            speaker.shutdown();
        }
        super.onDestroy();
    }

    private void tryLoadScripts() {
        try {
            loadScripts();
        } catch (IOException e) {
            Log.e("conversation", "Failed to load scripts directory");
        }
    }

    private void loadScripts() throws IOException {
        final String directory = "scripts";

        AssetManager assets = getAssets();
        for (String filename : assets.list(directory)) {
            Log.d("conversation", "Loading " + filename);

            final String path = String.format("%s/%s", directory, filename);
            InputStream scriptStream = assets.open(path);
            loadScript(scriptStream);
            scriptStream.close();
        }
        Log.d("conversation", scriptEngine.getTopicsString());
        Log.d("conversation", scriptEngine.getSortedString());
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

    private void processUserSentence() {
        String sentence = userSentenceInput.getText().toString();
        processReply(sentence);
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
                processReply(sentence);
            }
        }
    }

    private void processReply(final String sentence) {
        if (!"".equals(sentence)) {
            final String reply = scriptEngine.reply("localuser", sentence);
            conversation.append(String.format("you> %s\n", sentence));
            conversation.append(String.format("bot> %s\n", reply));
            speaker.speak(reply, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
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
