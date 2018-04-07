package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Context;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = layoutInflater.inflate(R.layout.word_list_layout, parent, false);

        TextView titleTextView = (TextView) rowView.findViewById(R.id.word_list_title);
        TextView incorrectTextView = (TextView) rowView.findViewById(R.id.word_list_incorrect);
        TextView correctTextView = (TextView) rowView.findViewById(R.id.word_list_correct);
        ImageView thumbnailImageView = (ImageView) rowView.findViewById(R.id.word_list_thumbnail);

        titleTextView.setText((String) getItem(position));
        incorrectTextView.setText("Incorrect: #");
        correctTextView.setText("Correct: #");

        return rowView;
    }
}
