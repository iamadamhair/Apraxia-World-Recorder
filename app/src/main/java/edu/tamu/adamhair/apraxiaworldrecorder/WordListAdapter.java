package edu.tamu.adamhair.apraxiaworldrecorder;

import android.app.Application;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.Repetition;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Word;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.WordViewModel;

/**
 * Created by adamhair on 4/7/2018.
 */

public class WordListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater;
    private ArrayList<Repetition> dataSource;
    private HashMap<String, Integer> imageResources;


    public WordListAdapter(Context context, ArrayList<Repetition> items) {
        mContext = context;
        dataSource = items;
        configureImageResources(items);
        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void configureImageResources(ArrayList<Repetition> items) {
        imageResources = new HashMap<String, Integer>();
        for (int i = 0; i < items.size(); i++) {
            String name = items.get(i).getWordName();
            int imageId;
            if (name.equals("break") || name.equals("case") || name.equals("switch")) {
                imageId = mContext.getResources().getIdentifier("edu.tamu.adamhair.apraxiaworldrecorder:drawable/" + name + "_ndp3", null, null);
            } else {
                imageId = mContext.getResources().getIdentifier("edu.tamu.adamhair.apraxiaworldrecorder:drawable/" + name, null, null);
            }
            imageResources.put(name, imageId);
        }
    }

    public int getImageId(int position) {return imageResources.get(getItem(position).getWordName()); }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Repetition getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.word_list_layout, parent, false);

            holder = new ViewHolder();

            holder.titleTextView = (TextView) convertView.findViewById(R.id.word_list_title);
            holder.incorrectTextView = (TextView) convertView.findViewById(R.id.word_list_incorrect);
            holder.correctTextView = (TextView) convertView.findViewById(R.id.word_list_correct);
            holder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.word_list_thumbnail);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TextView titleTextView = holder.titleTextView;
        TextView correctTextView = holder.correctTextView;
        TextView incorrectTextView = holder.incorrectTextView;
        ImageView thumbnailImageView = holder.thumbnailImageView;

        String title = getItem(position).getWordName();
        title = title.substring(0,1).toUpperCase() + title.substring(1);
        titleTextView.setText(title);

        Log.d("Word List", title);

        incorrectTextView.setText("Incorrect: " +
                String.valueOf(getItem(position).getNumIncorrect()));
        correctTextView.setText("Correct: " + String.valueOf(getItem(position).getNumCorrect()));

        thumbnailImageView.setImageResource(getImageId(position));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {switchToWordRecorderActivity(position);}
        });

        return convertView;
    }

    public void addItems(List<Repetition> repetitions) {
        this.dataSource = new ArrayList(repetitions);
        configureImageResources(this.dataSource);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        public TextView titleTextView;
        public TextView correctTextView;
        public TextView incorrectTextView;
        public ImageView thumbnailImageView;
        public Word Word;
    }

    private void switchToWordRecorderActivity(int position) {
        Intent intent = new Intent(mContext, WordRecorderActivity.class);
        intent.putExtra("title", getItem(position).getWordName());
        intent.putExtra("imageId", getImageId(position));
        intent.putExtra("userId", getItem(position).getUserId());
        intent.putExtra("wordId", getItem(position).getWordId());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
