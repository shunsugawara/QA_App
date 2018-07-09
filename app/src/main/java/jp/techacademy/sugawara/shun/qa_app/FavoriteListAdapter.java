package jp.techacademy.sugawara.shun.qa_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FavoriteListAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater = null;
    private ArrayList<Question> mQuestionArrayList;

    public FavoriteListAdapter(Context context){
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount(){
        return mQuestionArrayList.size();
    }

    @Override
    public int getViewTypeCount(){
        return 2;
    }

    @Override
    public Object getItem(int position){
        return mQuestionArrayList.get(position);
    }
    @Override
    public long getItemId(int position){
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.list_favorite,parent,false);
        }

        TextView titleText = (TextView) convertView.findViewById(R.id.titleTextView);
        titleText.setText(mQuestionArrayList.get(position).getTitle());

        TextView nameText = (TextView)convertView.findViewById(R.id.nameTextView);
        nameText.setText(mQuestionArrayList.get(position).getName());

        TextView resText = (TextView)convertView.findViewById(R.id.resTextView);
        int resNum = mQuestionArrayList.get(position).getAnswers().size();
        resText.setText(String.valueOf(resNum));

        byte[] bytes = mQuestionArrayList.get(position).getImageBytes();
        if(bytes.length != 0){
            Bitmap image = BitmapFactory.decodeByteArray(bytes,0,bytes.length).copy(Bitmap.Config.ARGB_8888,true);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
            imageView.setImageBitmap(image);
        }

        //追加
        TextView genreTextVIew = (TextView) convertView.findViewById(R.id.genreTextView);

        String genreText;
        switch(mQuestionArrayList.get(position).getGenre()){
            case 1 : genreText = "趣味"; break;
            case 2 : genreText = "生活"; break;
            case 3 : genreText = "健康"; break;
            case 4 : genreText = "コンピュータ"; break;
            default:genreText = "不明"; break;
        }
        genreTextVIew.setText(genreText);

        return convertView;
    }

    public void setQuestionArrayList(ArrayList<Question> questionArrayList){
        mQuestionArrayList = questionArrayList;
    }


}
