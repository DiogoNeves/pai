package com.mindfulst.pai.scripting;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.rivescript.RiveScript;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * RiveScript implementation on Android.
 * It works pretty much the same as the super class RiveScript but replaces
 * script loading and debug logging.
 */
public class AndroidRiveScript extends RiveScript {
    private WeakReference<Context> contextRef;

    public AndroidRiveScript(Context context) {
        super();
        contextRef = new WeakReference<>(context);
    }

    public AndroidRiveScript(Context context, boolean debug) {
        super(debug);
        contextRef = new WeakReference<>(context);
    }

    @Override
    public boolean loadDirectory(String path, String[] exts) {
        AssetManager assets = getAssets();
        if (assets != null) {
            Set<String> extensions = getExtensionSet(exts);
            try {
                return loadDirectoryImpl(assets, path, extensions);
            } catch (IOException e) {
                return error("Couldn't read any files from directory " + path);
            }
        }

        return false;
    }

    private AssetManager getAssets() {
        Context ctx = contextRef.get();
        if (ctx != null) {
            return ctx.getAssets();
        } else {
            cry("Context reference is null");
            return null;
        }
    }

    private Set<String> getExtensionSet(String[] exts) {
        Set<String> extensions = new HashSet<>(exts.length, 1.0f);
        for (String ext : exts) {
            // Use this util so it normalises the '.'
            extensions.add(FilenameUtils.getExtension(ext));
        }
        return extensions;
    }

    private boolean loadDirectoryImpl(AssetManager assets, String path,
                                      Set<String> exts) throws IOException {
        say("Load directory: " + path);

        for (String filename : assets.list(path)) {
            String extension = FilenameUtils.getExtension(filename);
            if (exts.contains(extension)) {
                String filepath = FilenameUtils.concat(path, filename);
                loadFile(filepath);
            }
        }

        say("Finished load directory: " + path);
        return true;
    }

    @Override
    public boolean loadFile(String path) {
        say("Load file: " + path);

        AssetManager assets = getAssets();
        if (assets != null) {
            try {
                return loadFileImpl(assets, path);
            } catch (IOException e) {
                return error(path + ": Failed to read");
            }
        }

        return false;
    }

    private boolean loadFileImpl(AssetManager assets, String path)
            throws IOException {
        InputStream fileStream = assets.open(path);
        InputStreamReader streamReader = new InputStreamReader(fileStream);
        BufferedReader reader = new BufferedReader(streamReader);

        Vector<String> lines = new Vector<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }

        reader.close();

        String[] code = com.rivescript.Util.Sv2s(lines);
        return parse(path, code);
    }

    @Override
    protected void println(String line) {
        Log.i("RS", line);
    }

    @Override
    protected void say(String line) {
        Log.d("RS", line);
    }

    @Override
    protected void cry(String line) {
        Log.w("RS", line);
    }

    @Override
    protected void cry(String text, String filename, int line) {
        Log.w("RS", String.format("%s at %s line %d.", line, filename, line));
    }

    @Override
    protected void trace(IOException e) {
        Log.wtf("RS", e);
    }
}
