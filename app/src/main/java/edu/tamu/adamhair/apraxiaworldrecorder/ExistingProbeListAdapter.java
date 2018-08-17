package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.Probe;

public class ExistingProbeListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater;
    private List<Probe> dataSource;
    private String username;

    public ExistingProbeListAdapter(Context context, List<Probe> items, String username) {
        mContext = context;
        dataSource = items;
        this.username = username;
        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        probeNameTextView.setText(getItem(position).getProbeDate());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToProbeActivity(position);
            }
        });

        return convertView;
    }

    public void addItems(List<Probe> probes) {
        this.dataSource = probes;
        notifyDataSetChanged();
    }

    private void switchToProbeActivity(int position) {
        Intent intent = new Intent(mContext, WordProbeActivity.class);
        intent.putExtra("probeNum", getItem(position).getProbeNumber());
        intent.putExtra("username", username);
        intent.putExtra("userId", getItem(position).getUserId());
        intent.putExtra("newProbe", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private static class ViewHolder {
        public TextView probeNameTextView;
    }
}
