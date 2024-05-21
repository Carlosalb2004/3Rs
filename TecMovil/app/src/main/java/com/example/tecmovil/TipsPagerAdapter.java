package com.example.tecmovil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class TipsPagerAdapter extends PagerAdapter {

    private Context context;
    private String[] tips;

    public TipsPagerAdapter(Context context, String[] tips) {
        this.context = context;
        this.tips = tips;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tipo, container, false);
        TextView textViewTip = view.findViewById(R.id.textViewTip);
        textViewTip.setText(tips[position]);
        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return tips.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}

