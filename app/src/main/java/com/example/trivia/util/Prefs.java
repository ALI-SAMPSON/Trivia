package com.example.trivia.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private SharedPreferences mPreferences;

    public Prefs(Activity activity) {
        mPreferences = activity.getPreferences(activity.MODE_PRIVATE);
    }

    public void saveHighScore(int score){
        int currentScore = score;
        int lastScore = mPreferences.getInt("high_score", 0);
        if(currentScore > lastScore){
            // save new high score
            mPreferences.edit().putInt("high_score", currentScore).apply();
        }

    }

    public int getHighScore(){
        return mPreferences.getInt("high_score", 0);
    }

    public void setState(int index){
        mPreferences.edit().putInt("index_state", index).apply();
    }

    public int getState(){
        return mPreferences.getInt("index_state", 0);
    }

}
