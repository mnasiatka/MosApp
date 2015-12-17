package com.finalproject.mosapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private Context mContext;
    private Activity mActivity;
    private List<Bitmap> mDataSet;
    private Map<Integer,ViewHolder> mHolderSet;

    public MainAdapter(Context context, List<Bitmap> dataSet) {
        mContext = context;
        mDataSet = dataSet;
        mHolderSet = new HashMap<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Picasso.with(mContext).load(R.drawable.loading).into(holder.image);

        holder.bmp = mDataSet.get(position);
        holder.image.setImageBitmap(mDataSet.get(position));
        if (MainActivity2.includePhoto.contains(position)) { // should include, show check
            holder.include.setImageResource(R.drawable.check2);
            holder.image.setAlpha(1f);
        } else { // not in include set, show x
            holder.include.setImageResource(R.drawable.xcheck);
            holder.image.setAlpha(.3f);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void remove(int position) {
        mDataSet.remove(position);
        notifyItemRemoved(position);
    }

    public void add(Bitmap bmp, int position) {
        mDataSet.add(position, bmp);
        notifyItemInserted(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image, include;
        public Bitmap bmp;
        public boolean isChecked;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            include = (ImageView) itemView.findViewById(R.id.include);
            isChecked = true;
        }
    }
}
