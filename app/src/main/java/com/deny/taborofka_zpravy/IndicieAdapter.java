package com.deny.taborofka_zpravy;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.progress.Taborofka.R;

import java.util.ArrayList;


public class IndicieAdapter extends ArrayAdapter<Indicie> {

    Context context;
    private ArrayList<Indicie> indicies;

    public IndicieAdapter(Context context, int textViewResourceId, ArrayList<Indicie> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.indicies = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.indicie, null);
        }
        Indicie o = indicies.get(position);
        if (o != null) {
            TextView predmet = (TextView) v.findViewById(R.id.textindicie);

            predmet.setText(String.valueOf(o.getsTexty().get(0)));
          }
        return v;
    }
}
