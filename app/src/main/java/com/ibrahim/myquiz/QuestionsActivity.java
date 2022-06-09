package com.ibrahim.myquiz;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArrayMap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.graphics.Color.GREEN;
import static com.ibrahim.myquiz.SetsActivity.setsIDs;
import static com.ibrahim.myquiz.SplashActivity.catList;
import static com.ibrahim.myquiz.SplashActivity.selected_cat_index;

public class QuestionsActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView question, qCount, timer;
    private Button option1, option2, option3, option4;
    private List<Question> questionList;
    private int questNum;
    private CountDownTimer countDown;
    private  int score;
    private FirebaseFirestore firestore;
    private int setNo;
    private Dialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        question = findViewById(R.id.question);
        qCount = findViewById(R.id.quest_num);
        timer = findViewById(R.id.count_down);

        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        option1.setOnClickListener(this);
        option2.setOnClickListener(this);
        option3.setOnClickListener(this);
        option4.setOnClickListener(this);

        loadingDialog = new Dialog(QuestionsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();

        questionList = new ArrayList<>();

        setNo = getIntent().getIntExtra("SETNO", 1);
        firestore = FirebaseFirestore.getInstance();

        getQuestionsList();
    }


    private void getQuestionsList()
    {
        questionList.clear();

        loadingDialog.show();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId())
                .collection(setsIDs.get(setNo)).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();

                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots)
                        {
                            docList.put(doc.getId(),doc);
                        }

                        QueryDocumentSnapshot quesListDoc  = docList.get("QUESTIONS_LIST");

                        String count = quesListDoc.getString("COUNT");

                        for(int i=0; i < Integer.valueOf(count); i++)
                        {
                            String quesID = quesListDoc.getString("Q" + String.valueOf(i+1) + "_ID");

                            QueryDocumentSnapshot quesDoc = docList.get(quesID);

                            questionList.add(new Question(

                                    quesDoc.getString("QUESTION"),
                                    quesDoc.getString("A"),
                                    quesDoc.getString("B"),
                                    quesDoc.getString("C"),
                                    quesDoc.getString("D"),
                                    Integer.valueOf(quesDoc.getString("ANSWER"))
                            ));

                        }

                        setQuestion();

                        loadingDialog.dismiss();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

       /* questionList.add(new Question("Question 1", "A", "B", "C", "D", 2));
        questionList.add(new Question("Question 2", "B", "C", "D", "A", 2));
        questionList.add(new Question("Question 3", "C", "B", "A", "D", 2));
        questionList.add(new Question("Question 4", "A", "D", "C", "B", 2));
        questionList.add(new Question("Question 5", "C", "D", "B", "A", 2));*/

    }
    private  void setQuestion()
    {
        timer.setText(String.valueOf(10));

        question.setText(questionList.get(0).getQuestion());
        option1.setText(questionList.get(0).getOptionA());
        option2.setText(questionList.get(0).getOptionB());
        option3.setText(questionList.get(0).getOptionC());
        option4.setText(questionList.get(0).getOptionD());

        qCount.setText(String.valueOf(1)+"/"+String.valueOf(questionList.size()));

        startTimer();
        questNum=0;
    }

    private  void startTimer(){
         countDown = new CountDownTimer(31000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished<60000)
                    timer.setText(String.valueOf(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
               changeQuestion();
            }
        };
        countDown.start();
    }
    @Override
    public  void onClick(View v)
    {
        int selectedOption=0;

        switch (v.getId())
        {
            case R.id.option1:
                selectedOption=1;
                break;
            case R.id.option2:
                selectedOption=2;
                break;
            case R.id.option3:
                selectedOption=3;
                break;
            case R.id.option4:
                selectedOption=4;
                break;
                default:
        }
        countDown.cancel();

        checkAnswer(selectedOption, v);
    }


    private  void checkAnswer(int selectedOption, View view)
    {
        if(selectedOption == questionList.get(questNum).getCorrectAns())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((Button)view).setBackgroundTintList(ColorStateList.valueOf(GREEN));

                score++;
            }

        }

        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.RED));

            }

            switch (questionList.get(questNum).getCorrectAns())
            {
                case 1:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        option1.setBackgroundTintList(ColorStateList.valueOf(GREEN));

                    }
                    break;
                case 2:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        option2.setBackgroundTintList(ColorStateList.valueOf(GREEN));

                    }
                    break;
                case 3:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        option3.setBackgroundTintList(ColorStateList.valueOf(GREEN));

                    }
                    break;
                case 4:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        option4.setBackgroundTintList(ColorStateList.valueOf(GREEN));

                    }
                    break;
            }

        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeQuestion();
            }
        }, 2000);

    }
    private void changeQuestion()
    {
        if (questNum<questionList.size()-1)
        {
            questNum++;

            playAnim(question,0,0);
            playAnim(option1,0,1);
            playAnim(option2,0,2);
            playAnim(option3,0,3);
            playAnim(option4,0,4);

            qCount.setText(String.valueOf(questNum+1) +"/"+String.valueOf(questionList.size()));
            timer.setText(String.valueOf(10));
            startTimer();

        }
        else
        {
                //goto score activity
                Intent intent = new Intent(QuestionsActivity.this, ScoreActivity.class);
                intent.putExtra("SCORE", String.valueOf( score) + " of " + String.valueOf(questionList.size()));
                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                QuestionsActivity.this.finish();




        }
    }
    private void playAnim(final View view, final int value, final int viewNum)
    {

        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500)
                .setStartDelay(100).setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }


                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (value==0)
                        {
                            switch (viewNum)
                            {
                                case 0:
                                    ((TextView)view).setText(questionList.get(questNum).getQuestion());
                                    break;
                                case 1:
                                    ((Button)view).setText(questionList.get(questNum).getOptionA());
                                    break;
                                case 2:
                                    ((Button)view).setText(questionList.get(questNum).getOptionB());
                                    break;
                                case 3:
                                     ((Button)view).setText(questionList.get(questNum).getOptionC());
                                    break;
                                case 4:
                                     ((Button)view).setText(questionList.get(questNum).getOptionD());
                                    break;
                            }

                            if (viewNum!=0)
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ccdafa")));



                                }
                            }

                            playAnim(view,1,viewNum);
                        }

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        countDown.cancel();
    }
}
