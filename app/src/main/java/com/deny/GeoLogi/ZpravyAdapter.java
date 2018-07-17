package com.deny.GeoLogi;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.deny.GeoLogi.R;

import java.util.ArrayList;


public class ZpravyAdapter extends ArrayAdapter<Zprava> {

    Context context;
    private ArrayList<Zprava> zpravy;

    public ZpravyAdapter(Context context, int textViewResourceId, ArrayList<Zprava> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.zpravy = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.zprava, null);
        }
        Zprava o = zpravy.get(position);
        if (o != null) {
            TextView predmet = (TextView) v.findViewById(R.id.predmet);
            if (!o.getbRead()) {predmet.setTypeface(null, Typeface.BOLD);}
            else {predmet.setTypeface(null, Typeface.NORMAL);}

            predmet.setTextSize(TypedValue.COMPLEX_UNIT_DIP,36);
            predmet.setTextColor( 0xFF000000 + o.getiBarva());
            predmet.setText(String.valueOf(o.getsPredmet()));
          }
        return v;
    }
}
