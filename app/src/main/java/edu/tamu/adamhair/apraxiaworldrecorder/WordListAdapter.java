package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by adamhair on 4/7/2018.
 */

public class WordListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater;
    private ArrayList<String> dataSource;

    public WordListAdapter(Context context, ArrayList<String> items) {
        mContext = context;
        dataSource = items;
        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Object getItem(int position) {
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

        String title = (String) getItem(position);
        title = title.substring(0,1).toUpperCase() + title.substring(1);
        titleTextView.setText(title);
        incorrectTextView.setText("Incorrect: #");
        correctTextView.setText("Correct: #");
        int imageId;
        // Reserved Java keywords
        if (title.equals("Break") || title.equals("Case") || title.equals("Switch")) {
            imageId = mContext.getResources().getIdentifier("edu.tamu.adamhair.apraxiaworldrecorder:drawable/" + title.toLowerCase() + "_ndp3", null, null);
        } else {
            imageId = mContext.getResources().getIdentifier("edu.tamu.adamhair.apraxiaworldrecorder:drawable/" + title.toLowerCase(), null, null);
        }

        thumbnailImageView.setImageResource(imageId);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {switchToWordRecorderActivity(position);}
        });

        return convertView;
    }

    private static class ViewHolder {
        public TextView titleTextView;
        public TextView correctTextView;
        public TextView incorrectTextView;
        public ImageView thumbnailImageView;
    }

    private void switchToWordRecorderActivity(int position) {
        Intent intent = new Intent(mContext, WordRecorderActivity.class);
        intent.putExtra("title", (String) getItem(position));
        mContext.startActivity(intent);
    }
}
