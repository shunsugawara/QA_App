package jp.techacademy.sugawara.shun.qa_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity
                implements View.OnClickListener,DatabaseReference.CompletionListener{

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private ProgressDialog mProgress;
    private FloatingActionButton mFavoriteFab;
    private DatabaseReference mAnswerRef;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map =(HashMap) dataSnapshot.getValue();
            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()){
                if(answerUid.equals(answer.getAnswerUid())){
                    return;
                }
            }

            String body = (String)map.get("body");
            String name = (String)map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body,name,uid,answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");
        setTitle(mQuestion.getTitle());

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("登録");

        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this,mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//                if(user == null){
//                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
//                    startActivity(intent);
//                }else{
//                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
//                    intent.putExtra("question", mQuestion);
//                    startActivity(intent);
//                }
//
//            }
//        });

        mFavoriteFab = (FloatingActionButton) findViewById(R.id.favoriteFab);
        mFavoriteFab.setOnClickListener(this);


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = databaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getQuestionUid())).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

    }

    @Override
    public void onComplete(DatabaseError databaseError,DatabaseReference databaseReference){
        mProgress.dismiss();
        if(databaseError == null){
            mFavoriteFab.setImageResource(R.drawable.ic_yellowstar);
        }else{
            Log.d("Fabkakunin",String.valueOf(databaseError));
        }

    }

    @Override
    public void onClick(View v){
        if(v.getId() == R.id.fab){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(user == null){
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }else {
                Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                intent.putExtra("question", mQuestion);
                startActivity(intent);
            }

        }else if(v.getId() ==  R.id.favoriteFab){

            String name = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference userRef = databaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.FavUserPATH);

            HashMap<String,String> data = new HashMap<String,String>();
            data.put("user",name);

            userRef.push().setValue(data,this);
            mProgress.show();
        }

    }

}
