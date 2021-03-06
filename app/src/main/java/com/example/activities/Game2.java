package com.example.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Game2 extends AppCompatActivity {

    ArrayList<String> gameCluesNames = new ArrayList<String>();
    Controller controller;
    WebView webView;
    ImageView answerRef;
    Boolean showClue = false;
    Boolean finishGame = false;
    Boolean gifDisplayed = false;

    public void main() {
        gifDisplayed = false;
        showClue = false;
        if (controller.roundIterator < gameCluesNames.size()) {
            displayRoundScreen();
        } else {
            setWrapUpScreen();
            finishGame = true;
        }
    }

    public void displayRoundScreen() {
        controller.displayRoundScreen();
        webView.animate().alpha(0f).setDuration(500);
        answerRef.animate().alpha(0f).setDuration(500);
        setValuesRoundScreen();
    }

    public void displayGameScreen(View view) {
        if (finishGame) {
            moveToNextActivity();
        } else {
            controller.displayGameScreen(view);
            answerRef.animate().alpha(1f).setDuration(500);
            setValuesGameScreen();
        }
    }

    public void setValuesRoundScreen() {
        controller.setValuesRoundScreen();
    }

    public void setValuesGameScreen() {
        String clueName = gameCluesNames.get(controller.roundIterator - 1);
        controller.clueRef.setText("Show clue");
        displayGif(clueName);
    }

    public void setWrapUpScreen() {
        String textForFinish;

        if (controller.score.get("A") > controller.score.get("B")) {
            textForFinish = "Team A won. \n Congratulation!. \n\n Tap below to start a new game.";
        } else if (controller.score.get("A") < controller.score.get("B")) {
            textForFinish = "Team B won. \n Congratulation!. \n\n Tap below to start a new game.";
        } else {
            textForFinish = "It's a draw. \n Congratulation to everyone! \n\n Tap below to start a new game.";
        }
        controller.t1ScoreRef.setText("Team A: " + Math.round(controller.score.get("A") * 10) / 10.0);
        controller.t2ScoreRef.setText("Team B: " + Math.round(controller.score.get("B") * 10) / 10.0);
        controller.correctRef.animate().alpha(0f).setDuration(500);
        webView.animate().alpha(0f).setDuration(500);
        controller.wrongRef.animate().alpha(0f).setDuration(500);
        controller.startRef.animate().alpha(1f).setDuration(500);
        controller.clueRef.setText("Game finished.");
        controller.phaseDescRef.animate().alpha(1f).setDuration(500);
        controller.phaseDescRef.setText(textForFinish);

    }

    public void moveToNextActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void correctButtonPressed(View view) {
        controller.correctButtonPressed(view);
        main();
    }

    public void wrongButtonPressed(View view) {
        controller.wrongButtonPressed(view);
        main();
    }

    public void exitGame(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void displayGif(String clue) {
//        Retrieve gif url
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GifApi.BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .build();

        GifApi api = retrofit.create(GifApi.class);
        Call<Gif> call = api.getGif(clue, "1", "0", "g", "en");
        Log.i("GAME 2", "API for clue: " + clue);

        call.enqueue(new Callback<Gif>() {
            @Override
            public void onResponse(Call<Gif> call, Response<Gif> response) {
                Gif gif = response.body();
                ArrayList<Data> data = gif.getData();
                String gifUrl = data.get(0).getEmbed_url();
                Log.i("GIF", "" + gifUrl);

                //        Display gif
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webView.loadUrl(gifUrl);
            }

            @Override
            public void onFailure(Call<Gif> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

//        Wait with displaying gif until the new one is loaded
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (webView.getProgress() == 100) {
                    webView.animate().alpha(1f).setDuration(500);
                    gifDisplayed = true;
                }
            }
        });
    }

    public void showText(View view) {
        if (gifDisplayed) {
            if (!showClue) {
                String clueName = gameCluesNames.get(controller.roundIterator - 1);
                controller.clueRef.setText(clueName);
                showClue = true;
                controller.score.put(controller.whoseTurn, controller.score.get(controller.whoseTurn) - 1.0);
                controller.t1ScoreRef.setText("Team A: " + Math.round(controller.score.get("A") * 10) / 10.0);
                controller.t2ScoreRef.setText("Team B: " + Math.round(controller.score.get("B") * 10) / 10.0);
            }
//            else {
//                controller.clueRef.setText("Show clue");
//                showClue = false;
//            }
        }
    }

    public void showAnswer(View view) {
        String clueName = gameCluesNames.get(controller.roundIterator - 1);
        Toast.makeText(this, clueName, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2);

        controller = new Controller();
        webView = (WebView) findViewById(R.id.gif_screen);
        answerRef = (ImageView) findViewById(R.id.answer);

        controller.correctRef = (ImageView) findViewById(R.id.correct);
        controller.wrongRef = (ImageView) findViewById(R.id.wrong);
        controller.startRef = (ImageView) findViewById(R.id.start_game);
        controller.turnRef = (TextView) findViewById(R.id.whose_turn);
        controller.clueRef = (TextView) findViewById(R.id.clue);
        controller.t1ScoreRef = (TextView) findViewById(R.id.t1_score);
        controller.t2ScoreRef = (TextView) findViewById(R.id.t2_score);
        controller.phaseDescRef = (TextView) findViewById(R.id.phaseDesc);
        controller.exitRef = (ImageView) findViewById(R.id.exit);

//        Set background color for gif screen
        webView.setBackgroundColor(Color.parseColor("#104C7A"));

//        Get data from previous activity
        Intent intent = getIntent();
        gameCluesNames = (ArrayList<String>) intent.getStringArrayListExtra("gameClues");
        Log.i("GAME 2", "Clues: " + gameCluesNames);
        controller.score.put("A", intent.getDoubleExtra("gameScoreA", 0.0));
        controller.score.put("B", intent.getDoubleExtra("gameScoreB", 0.0));
        controller.teamSize.put("A", intent.getIntExtra("teamASize", 1));
        controller.teamSize.put("B", intent.getIntExtra("teamBSize", 1));

        Log.i("GAME 2", "Team A size " + intent.getIntExtra("teamASize", 1));
        Log.i("GAME 2", "Team B size " + intent.getIntExtra("teamBSize", 1));

        if (controller.teamSize.get(("A")) > controller.teamSize.get(("B"))) {
//                    In controller will be changed to A
            controller.whoseTurn = "B";
        } else {
            controller.whoseTurn = "A";
        }
        setValuesRoundScreen();
        Log.i("GAME 2", "Game 2 started");
    }
}