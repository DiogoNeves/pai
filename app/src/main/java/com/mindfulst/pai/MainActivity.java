package com.mindfulst.pai;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rivescript.RiveScript;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity {

    RiveScript scriptEngine;

    TextView conversation;
    TextView userSentence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scriptEngine = new RiveScript(false);
        scriptEngine.setHandler("perl", new com.rivescript.lang.Perl(scriptEngine,
                "assets/lang/rsp4j.pl"));

        tryLoadScripts();

        conversation = (TextView) findViewById(R.id.outConversation);
        userSentence = (TextView) findViewById(R.id.inUserSentence);

        final Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new SendButtonListener());
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

    public class SendButtonListener implements View.OnClickListener {
        public void onClick(View v) {
            if (userSentence.getText().length() > 0) {
                final String sentence = userSentence.getText().toString();
                final String reply = scriptEngine.reply("localuser", sentence);

                conversation.append(String.format("you> %s\n", sentence));
                conversation.append(String.format("bot> %s\n", reply));
            }
        }
    }
}
