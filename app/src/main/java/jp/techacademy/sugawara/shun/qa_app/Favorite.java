package jp.techacademy.sugawara.shun.qa_app;

import java.io.Serializable;

public class Favorite implements Serializable {
    private String mGenre;
    private String mQuestionUid;
    private String mfavoriteKey;

    public Favorite(String genre, String questionUid, String favoriteKey) {
        mGenre = genre;
        mQuestionUid = questionUid;
        mfavoriteKey = favoriteKey;
    }

    public String getmGenre() {
        return mGenre;
    }

    public String getmQuestionUid() {
        return mQuestionUid;
    }

    public String getmfavoriteKey() {
        return mfavoriteKey;
    }
}