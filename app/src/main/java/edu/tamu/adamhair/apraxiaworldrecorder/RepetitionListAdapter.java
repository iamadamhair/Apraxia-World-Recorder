package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by adamhair on 4/9/2018.
 */

public class RepetitionListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater;
    private ArrayList<String> dataSource;

    public RepetitionListAdapter(Context context, ArrayList<String> items) {
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
        RepetitionViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.repetition_list_layout, parent, false);

            holder = new RepetitionViewHolder();

            holder.countTextView = (TextView) convertView.findViewById(R.id.repetitionCountTextView);
            holder.correctCheckBox = (CheckBox) convertView.findViewById(R.id.repetitionCorrectCheckBox);
            holder.incorrectCheckBox = (CheckBox) convertView.findViewById(R.id.repetitionIncorrectCheckBox);
            holder.recordButton = (Button) convertView.findViewById(R.id.repetitionRecordButton);
            holder.playButton = (Button) convertView.findViewById(R.id.repetitionPlayButton);

            convertView.setTag(holder);
        } else {
            holder = (RepetitionViewHolder) convertView.getTag();
        }

        TextView countTextView = holder.countTextView;
        countTextView.setText((String) getItem(position));

//        TextView titleTextView = holder.titleTextView;
//        TextView correctTextView = holder.correctTextView;
//        TextView incorrectTextView = holder.incorrectTextView;
//        ImageView thumbnailImageView = holder.thumbnailImageView;
//
//        String title = (String) getItem(position);
//        title = title.substring(0,1).toUpperCase() + title.substring(1);
//        titleTextView.setText(title);
//        incorrectTextView.setText("Incorrect: #");
//        correctTextView.setText("Correct: #");


        return convertView;
    }



    private static class RepetitionViewHolder {
        public TextView countTextView;
        public CheckBox correctCheckBox;
        public CheckBox incorrectCheckBox;
        public Button recordButton;
        public Button playButton;
    }

}
