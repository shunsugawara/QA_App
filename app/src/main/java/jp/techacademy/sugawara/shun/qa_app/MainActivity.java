package jp.techacademy.sugawara.shun.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.security.Key;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private int mGenre = 0;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private DatabaseReference mFavoriteRef;
    private DatabaseReference mFavoriteListRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private ArrayList<String> mFavoriteArrayList;
    private QuestionsListAdapter mAdapter;
    private FavoriteListAdapter mFavoriteAdapter;
    private NavigationView mNavigationView;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap)dataSnapshot.getValue();
            String title = (String)map.get("title");
            String body = (String) map.get("body");
            String name =(String)map.get("name");
            String uid = (String)map.get("uid");
            String imageString = (String) map.get("image");

            byte[] bytes;
            if(imageString != null){
                bytes = Base64.decode(imageString,Base64.DEFAULT);
            }else{
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if(answerMap != null){
                for(Object key :answerMap.keySet()){
                    HashMap temp = (HashMap) answerMap.get((String)key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody,answerName,answerUid,(String) key);
                    answerArrayList.add(answer);
                }
            }

            Question question = new Question(title,body,name,uid,dataSnapshot.getKey(),mGenre,bytes,answerArrayList);
            mQuestionArrayList.add(question);
            mAdapter.notifyDataSetChanged();

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            for(Question question : mQuestionArrayList){
                if(dataSnapshot.getKey().equals(question.getQuestionUid())){
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if(answerMap != null){
                        for(Object key:answerMap.keySet()){
                            HashMap temp = (HashMap) answerMap.get((String)key);
                            String answerBody = (String)temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody,answerName,answerUid,(String)key);
                            question.getAnswers().add(answer);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) { }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

        @Override
        public void onCancelled(DatabaseError databaseError) { }
    };


    private ChildEventListener mFavoritesEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap)dataSnapshot.getValue();

                    if(map != null){
                String tempQId = (String) map.get("favQuestionId");
                mFavoriteArrayList.add(tempQId);
            }
       }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) { }
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
        @Override
        public void onCancelled(DatabaseError databaseError) { }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mGenre == 0 ){
                    Snackbar.make(view,"ジャンルを選択してください",Snackbar.LENGTH_LONG).show();
                    return;
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user == null){
                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getApplicationContext(),QuestionSendActivity.class);
                    intent.putExtra("genre",mGenre);
                    startActivity(intent);

                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,mToolbar,R.string.app_name,R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);


        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mListView = (ListView)findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);

        //add
        mFavoriteAdapter = new FavoriteListAdapter(this);
        ////
        mQuestionArrayList = new ArrayList<Question>();
        mFavoriteArrayList = new ArrayList<String>();
        mAdapter.notifyDataSetChanged();
        mFavoriteAdapter.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent,View view, int position, long id){
                Intent intent = new Intent(getApplicationContext(),QuestionDetailActivity.class);
                intent.putExtra("question",mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();

        if(mGenre ==0){
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            onNavigationItemSelected(navigationView.getMenu().getItem(0));
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Menu menu = mNavigationView.getMenu();
            MenuItem menuItem1 = menu.findItem(R.id.nav_favorite);
            menuItem1.setVisible(false);
        }else {
            Menu menu = mNavigationView.getMenu();
            MenuItem menuItem1 = menu.findItem(R.id.nav_favorite);
            menuItem1.setVisible(true);
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
            Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.nav_hobby){
            mToolbar.setTitle("趣味");
            mGenre = 1;
        }else if(id == R.id.nav_life){
            mToolbar.setTitle("生活");
            mGenre = 2;
        }else if(id == R.id.nav_health){
            mToolbar.setTitle("健康");
            mGenre = 3;
        }else if (id == R.id.nav_computer){
            mToolbar.setTitle("コンピュータ");
            mGenre = 4;
        }else if (id == R.id.nav_favorite){
            mToolbar.setTitle("お気に入り");
            favoriteListShow();
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        mQuestionArrayList.clear();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        if(mGenreRef != null){
            mGenreRef.removeEventListener(mEventListener);
        }
        mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
        mGenreRef.addChildEventListener(mEventListener);

        return true;
    }



    //以下追加分
    private void favoriteListShow(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mQuestionArrayList.clear();
        mFavoriteArrayList.clear();
        mFavoriteAdapter.setQuestionArrayList(mQuestionArrayList);

        mListView.setAdapter(mFavoriteAdapter);

        if(mGenreRef != null){
            mGenreRef.removeEventListener(mEventListener);
        }
//        for(int tempGenre =1; tempGenre < 5;tempGenre++ ) {
//            mGenre = tempGenre;
//            mGenreRef = mDatabaseReference.child(Const.UserPATH).child(user.getUid()).child(Const.FavUserPATH);
//            mGenreRef.addValueEventListener(favoriteValueEventListener);
//
        mFavoriteRef = mDatabaseReference.child(Const.UserPATH).child(user.getUid()).child(Const.FavUserPATH);
        mFavoriteRef.addChildEventListener(mFavoritesEventListener);
//        mFavoriteRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                HashMap map = (HashMap)dataSnapshot.getValue();
//
//                if(map != null){
//                        String tempQId = (String) map.get("favQuestionId");
//                        mFavoriteArrayList.add(tempQId);
//                }
//            }
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) { }
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
//            @Override
//            public void onCancelled(DatabaseError databaseError) { }
//        });


        if(mFavoriteArrayList.size() > 0){
            mFavoriteListRef = mDatabaseReference.child(Const.ContentsPATH);
            mFavoriteListRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Query query = mFavoriteListRef.orderByKey().equalTo(mFavoriteArrayList.get(0));
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                            System.out.println("The dinosaur just shorter than the stegosaurus is: " + firstChild.getKey());
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // ...
                        }
                    });
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

//    private ChildEventListener mFavoriteEventListener = new ChildEventListener() {
//        @Override
//        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            HashMap map = (HashMap)dataSnapshot.getValue();
//            String title = (String)map.get("title");
//            String body = (String) map.get("body");
//            String name =(String)map.get("name");
//            String uid = (String)map.get("uid");
//            String imageString = (String) map.get("image");
//
//            byte[] bytes;
//            if(imageString != null){
//                bytes = Base64.decode(imageString,Base64.DEFAULT);
//            }else{
//                bytes = new byte[0];
//            }
//
//            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
//            ArrayList<String> favoriteUserList = new ArrayList<String>();
//            HashMap answerMap = (HashMap) map.get("answers");
//            if(answerMap != null){
//                for(Object key :answerMap.keySet()){
//                    HashMap temp = (HashMap) answerMap.get((String)key);
//                    String answerBody = (String) temp.get("body");
//                    String answerName = (String) temp.get("name");
//                    String answerUid = (String) temp.get("uid");
//                    Answer answer = new Answer(answerBody,answerName,answerUid,(String) key);
//                    answerArrayList.add(answer);
//                }
//            }
//
//            HashMap favoriteMap = (HashMap) map.get("favorites");
//            if(favoriteMap != null){
//                for(Object key : favoriteMap.keySet()){
//                    HashMap temp = (HashMap) favoriteMap.get((String)key);
//                    String tempUser1 = String.valueOf(temp.get("user"));
//                    String tempUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                    if(tempUser.equals(tempUser1)){
//                        Question question = new Question(title,body,name,uid,dataSnapshot.getKey(),mGenre,bytes,answerArrayList,favoriteUserList);
//                        mQuestionArrayList.add(question);
//                    }else{
//                        Log.d("wahwahwah",tempUser1);
//                        Log.d("wahwahwaha",tempUser);
//
//                    }
//                }
//            }
//
//            mFavoriteAdapter.notifyDataSetChanged();
//
//        }
//
//        @Override
//        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//            HashMap map = (HashMap) dataSnapshot.getValue();
//
//            for(Question question : mQuestionArrayList){
//                if(dataSnapshot.getKey().equals(question.getQuestionUid())){
//                    question.getAnswers().clear();
//                    HashMap answerMap = (HashMap) map.get("answers");
//                    if(answerMap != null){
//                        for(Object key:answerMap.keySet()){
//                            HashMap temp = (HashMap) answerMap.get((String)key);
//                            String answerBody = (String)temp.get("body");
//                            String answerName = (String) temp.get("name");
//                            String answerUid = (String) temp.get("uid");
//                            Answer answer = new Answer(answerBody,answerName,answerUid,(String)key);
//                            question.getAnswers().add(answer);
//                        }
//                    }
//                    mFavoriteAdapter.notifyDataSetChanged();
//                }
//            }
//
//        }
//
//        @Override
//        public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//        }
//
//        @Override
//        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//
//        }
//    };




}