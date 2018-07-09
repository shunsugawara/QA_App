package jp.techacademy.sugawara.shun.qa_app;

import java.io.Serializable;
import java.util.ArrayList;

public class FavoriteList implements Serializable{
    private String mUserId;
    private String mUid;
    private ArrayList<String> mFavoriteQuestionIdList;

    public String getUserId(){ return mUserId;}
    public String getUid(){return mUid;}

    public ArrayList<String> getmFavoriteQuestionIdList() {return mFavoriteQuestionIdList;}

    public FavoriteList(String userId,String uid, ArrayList<String> favoriteQuestionIdList){
        mUserId = userId;
        mUid = uid;
        mFavoriteQuestionIdList = favoriteQuestionIdList;
    }

}
