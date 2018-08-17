package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.Probe;

public class ProbeWordListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater;
    private List<Probe> dataSource;
    private String username;
    private FrameLayout frameLayout;
    private TextView probeWord;
    private RadioGroup probeLabel;
    private ImageView probeImage;

    public ProbeWordListAdapter(Context context, List<Probe> dataSource, String username) {
        mContext = context;
        this.dataSource = dataSource;
        this.username = username;
        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setUiElements(FrameLayout frameLayout, TextView probeWord, RadioGroup probeLabel, ImageView probeImage) {
        this.frameLayout = frameLayout;
        this.probeWord = probeWord;
        this.probeLabel = probeLabel;
        this.probeImage = probeImage;
    }

    public void addItems(List<Probe> probes) {
        this.dataSource = probes;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Probe getItem(int position) {
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
            convertView = layoutInflater.inflate(R.layout.probe_selection_layout, parent, false);

            holder = new ViewHolder();

            holder.probeNameTextView = convertView.findViewById(R.id.probeNameTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TextView probeNameTextView = holder.probeNameTextView;
        String title = getItem(position).getWordName();
        title = title.substring(0, 1).toUpperCase() + title.substring(1);
        probeNameTextView.setText(title);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFrameView(position);
            }
        });

        return convertView;
    }


    private void showFrameView(int position) {
        int imageId = mContext.getResources().getIdentifier("edu.tamu.adamhair.apraxiaworldrecorder:drawable/" + getItem(position).getWordName(), null, null);
        this.probeImage.setImageResource(imageId);

        String title = getItem(position).getWordName();
        title = title.substring(0, 1).toUpperCase() + title.substring(1);
        this.probeWord.setText(title);

        frameLayout.setVisibility(View.VISIBLE);
    }

    private static class ViewHolder {
        public TextView probeNameTextView;
    }
}
