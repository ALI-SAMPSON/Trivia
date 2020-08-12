package com.example.trivia.data;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.trivia.controller.AppController;
import com.example.trivia.model.Question;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class QuestionBank {

    private static final String TAG = QuestionBank.class.getSimpleName();

    private ArrayList<Question> mQuestionArrayList = new ArrayList<>();

    private String url = "https://raw.githubusercontent.com/curiousily/simple-quiz/master/script/statements-data.json";

    public List<Question> getQuestions(final AnswerListAsyncResponse callBack){

        JsonArrayRequest mJsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for(int i = 0; i < response.length(); i++){
                    try {
                        // set questions
                        Question mQuestion = new Question();
                        mQuestion.setAnswer(response.getJSONArray(i).getString(0));
                        mQuestion.setAnswerTrue(response.getJSONArray(i).getBoolean(1));
                        // add question object to arrraylist
                        mQuestionArrayList.add(mQuestion);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if(null != callBack){
                    callBack.processFinished(mQuestionArrayList);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        AppController.getInstance().addToRequestQueue(mJsonArrayRequest);

        return mQuestionArrayList;
    }

}
