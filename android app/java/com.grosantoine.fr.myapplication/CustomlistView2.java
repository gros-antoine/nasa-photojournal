package com.grosantoine.fr.myapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CustomListView2 extends ArrayAdapter<String> {

    private ArrayList<String> title;
    private ArrayList<Bitmap> img;
    private ArrayList<String> id;
    private ArrayList<String> date;
    private Activity context;

    public CustomListView2(Activity context, ArrayList<String> title, ArrayList<Bitmap> img, ArrayList<String> id, ArrayList<String> date) {
        super(context, R.layout.listview_layout2, title);

        this.context = context;
        this.title = title;
        this.img = img;
        this.id = id;
        this.date = date;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View r = convertView;
        ViewHolder viewHolder = null;

        if(r == null) {

            LayoutInflater layoutInflater = context.getLayoutInflater();
            r = layoutInflater.inflate(R.layout.listview_layout2, null, true);
            viewHolder = new ViewHolder(r);
            r.setTag(viewHolder);
        }

        else {
            viewHolder = (ViewHolder) r.getTag();
        }

        // On redimensionne l'image et l'ImageView
        Bitmap b = img.get(position);

        int w = b.getWidth();
        int h = b.getHeight();

        float px = 400 * context.getResources().getDisplayMetrics().density;
        float coeff = 0;

        if(w > h) {
            coeff = px/w;
        }

        else {
            coeff = px/h;
        }

        int new_w = (int) coeff*w;
        int new_h = (int) coeff*h;

        Bitmap resized = Bitmap.createScaledBitmap(b, new_w, new_h, false);

        // On charge les données dans les TextView et l'ImageView
        viewHolder.tvd.setText(date.get(position));
        viewHolder.tvw.setText(id.get(position) + " - " + title.get(position));
        viewHolder.ivw.setImageBitmap(resized);
        viewHolder.ivw.setClipToOutline(true);

        return r;
    }

    class ViewHolder {

        TextView tvd;
        TextView tvw;
        ImageView ivw;

        ViewHolder(View v) {

            tvd = (TextView) v.findViewById(R.id.date);
            tvw = (TextView) v.findViewById(R.id.title2);
            ivw = (ImageView) v.findViewById(R.id.image2);
        }
    }
}
