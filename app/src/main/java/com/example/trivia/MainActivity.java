package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trivia.data.AnswerListAsyncResponse;
import com.example.trivia.data.QuestionBank;
import com.example.trivia.model.Question;
import com.example.trivia.model.Score;
import com.example.trivia.util.Prefs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private TextView questionTextView, questionCounterTextView, questionScoreTextView, questionHighScoreTextView;
    private Button trueButton, falseButton;
    private ImageButton nextButton, previousButton;
    private int currentIndex = 0;
    private List<Question> mQuestionList;

    private static int mScoreCount = 0;

    private Score mScore;

    private static final String PREF_NAME = "SHARED_PREF";

    private Prefs mPrefs;

    private SoundPool mSoundPool;
    private MediaPlayer mMediaPlayer;
    private int mSoundCorrect, mSoundWrong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScore = new Score();

        mPrefs = new Prefs(MainActivity.this);

        questionTextView = findViewById(R.id.question_textView);
        questionCounterTextView = findViewById(R.id.counter_textView);
        questionScoreTextView= findViewById(R.id.score_textView);
        questionHighScoreTextView= findViewById(R.id.high_score_textView);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(this, R.raw.audio);
        playBackgroundMusic();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });

       setUpSoundPool();

        trueButton = findViewById(R.id.true_button);
        falseButton = findViewById(R.id.false_button);

        nextButton = findViewById(R.id.next_button);
        previousButton = findViewById(R.id.prev_button);

        trueButton.setOnClickListener(this);
        falseButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);

        mQuestionList = new ArrayList<>();

       new QuestionBank().getQuestions(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questions) {
                mQuestionList.addAll(questions);
                questionCounterTextView.setText(currentIndex + " / " + mQuestionList.size());
                questionTextView.setText(questions.get(currentIndex).getAnswer());
            }
        });

        questionScoreTextView.setText(MessageFormat.format("Current Score: {0}", mScore.getScore()));

        // get previous state
        currentIndex = mPrefs.getState();

        questionHighScoreTextView.setText(MessageFormat.format("High Score: {0}", mPrefs.getHighScore()));

    }

    private void setUpSoundPool(){
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .build();

        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();

        mSoundCorrect = mSoundPool.load(this, R.raw.complete, 1);
        mSoundWrong = mSoundPool.load(this, R.raw.defeat_two, 1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.next_button:
                moveToNext();
                break;
            case R.id.prev_button:
                if(currentIndex > 0){
                    currentIndex = (currentIndex - 1) % mQuestionList.size();
                    updateQuestion();
                }
                break;
            case R.id.true_button:
                checkAnswer(true);
                updateQuestion();
                break;
            case R.id.false_button:
                checkAnswer(false);
                updateQuestion();
                break;
        }
    }

    private void fadeView(){
        final CardView mCardView = findViewById(R.id.cardView);
        AlphaAnimation mAlphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        mAlphaAnimation.setDuration(350);
        mAlphaAnimation.setRepeatCount(1);
        mAlphaAnimation.setRepeatMode(Animation.REVERSE);
        mCardView.setAnimation(mAlphaAnimation);

        mAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mCardView.setCardBackgroundColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCardView.setCardBackgroundColor(Color.WHITE);
                moveToNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void shakeAnimation(){
        Animation shake = AnimationUtils.loadAnimation(this,
                R.anim.shake_animation);
        final CardView mCardView = findViewById(R.id.cardView);
        mCardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mCardView.setCardBackgroundColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCardView.setCardBackgroundColor(Color.WHITE);
                moveToNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void checkAnswer(boolean UserChoice) {
        boolean answerIsTrue = mQuestionList.get(currentIndex).isAnswerTrue();
        int toastMessageId = 0;
        if(UserChoice == answerIsTrue){
            fadeView();
            toastMessageId = R.string.correct_answer;
            // play sound if answer is true
            mSoundPool.play(mSoundCorrect, 1,1,0,0,1);
            addPoints();
        }else{
            shakeAnimation();
            toastMessageId = R.string.wrong_answer;
            // play sound if answer is false
            mSoundPool.play(mSoundWrong, 1,1,0,0,1);
            deductPoints();
        }
        Toast.makeText(this, toastMessageId, Toast.LENGTH_SHORT).show();
    }

    private void updateQuestion() {
        questionTextView.setText(mQuestionList.get(currentIndex).getAnswer());
        questionCounterTextView.setText(MessageFormat.format("{0} / {1}", currentIndex, mQuestionList.size()));
    }

    // add points
   private void addPoints(){
        mScoreCount += 100;
        mScore.setScore(mScoreCount);
        questionScoreTextView.setText(MessageFormat.format("Current Score: {0}", mScore.getScore()));

       Log.d(TAG, "addPoints: " + mScore.getScore());
   }

    // deduct points
    private void deductPoints(){
        mScoreCount -= 100;
        if(mScore.getScore() > 0){
            mScore.setScore(mScoreCount);
            questionScoreTextView.setText(MessageFormat.format("Current Score: {0}", mScore.getScore()));
        }else{
            mScoreCount = 0;
            mScore.setScore(mScoreCount);
            questionScoreTextView.setText(MessageFormat.format("Current Score: {0}", mScore.getScore()));
        }
        Log.d(TAG, "deductPoints: " + mScore.getScore());
    }

    // automatically move to the next page
    private void moveToNext(){
        currentIndex = (currentIndex + 1) % mQuestionList.size();
        updateQuestion();
    }

    @Override
    protected void onPause() {
        mPrefs.saveHighScore(mScore.getScore());
        mPrefs.setState(currentIndex);
        // pause background music
        pauseBackgroundMusic();
        super.onPause();
    }

    // pause background music
    private void pauseBackgroundMusic(){
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        }
    }

    // pause background music
    private void playBackgroundMusic(){
        if(!mMediaPlayer.isPlaying()){
            mMediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // release sound pool from memory
        if(mSoundPool != null){
            mSoundPool.release();
            mSoundPool = null;
        }
        if(mMediaPlayer != null){
            mMediaPlayer.pause();;
            mMediaPlayer.release();
        }
    }
}