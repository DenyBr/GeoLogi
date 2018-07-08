package com.deny.GeoLogi;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class ResultsAdapter extends ArrayAdapter<Result> {
    Context context;
    private ArrayList<Result> results;

    public ResultsAdapter(Context context, int textViewResourceId, ArrayList<Result> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.results = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.teamresult, null);
        }

        Result o = results.get(position);
        if (o != null) {
            TextView teamName = (TextView) v.findViewById(R.id.TeamName);

            teamName.setTextSize(TypedValue.COMPLEX_UNIT_DIP,36);
            teamName.setText(o.getsId());

            TextView hints = (TextView) v.findViewById(R.id.Hints);

            hints.setTextSize(TypedValue.COMPLEX_UNIT_DIP,36);
            hints.setText(o.getsHints());

            TextView points = (TextView) v.findViewById(R.id.Points);

            points.setTextSize(TypedValue.COMPLEX_UNIT_DIP,36);
            points.setText(o.getsPoints());
        }
        return v;
    }
}
