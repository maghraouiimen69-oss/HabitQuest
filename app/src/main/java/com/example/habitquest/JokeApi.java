package com.example.habitquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class JokeApi {

    public interface Callback {
        void onSuccess(Joke joke);

        void onError();
    }

    public static final class Joke {
        public final String setup;
        public final String punchline;
        public final String date;

        Joke(String setup, String punchline, String date) {
            this.setup = setup;
            this.punchline = punchline;
            this.date = date;
        }

        public boolean isToday() {
            return LocalDate.now().toString().equals(date);
        }
    }

    private static final String ENDPOINT = "https://v2.jokeapi.dev/joke/Any?type=twopart&safe-mode";
    private static final String PREFS = "habitquest_joke_cache";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private JokeApi() {
    }

    public static Joke cached(Context context) {
        SharedPreferences prefs = prefs(context);
        String setup = prefs.getString("joke_setup", null);
        if (setup == null) {
            return null;
        }
        return new Joke(setup,
                prefs.getString("joke_punchline", ""),
                prefs.getString("joke_date", ""));
    }

    public static void random(Context context, Callback callback) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            try {
                Joke joke = download();
                save(app, joke);
                MAIN.post(() -> callback.onSuccess(joke));
            } catch (Exception e) {
                MAIN.post(callback::onError);
            }
        });
    }

    private static Joke download() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.setRequestProperty("Accept", "application/json");
        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + connection.getResponseCode());
            }
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }
            JSONObject json = new JSONObject(body.toString());
            if (json.optBoolean("error")) {
                throw new IOException("API error");
            }
            return new Joke(json.getString("setup").trim(),
                    json.getString("delivery").trim(),
                    LocalDate.now().toString());
        } finally {
            connection.disconnect();
        }
    }

    private static void save(Context context, Joke joke) {
        prefs(context).edit()
                .putString("joke_setup", joke.setup)
                .putString("joke_punchline", joke.punchline)
                .putString("joke_date", joke.date)
                .apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
