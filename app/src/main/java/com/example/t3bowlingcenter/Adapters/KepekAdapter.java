package com.example.t3bowlingcenter.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.t3bowlingcenter.DataModels.Kepek;
import com.example.t3bowlingcenter.R;

import java.util.ArrayList;

public class KepekAdapter extends RecyclerView.Adapter<KepekAdapter.ViewHolder> {
    private ArrayList<Kepek> mKepekAdat;
    private ArrayList<Kepek> mKepekOsszesAdat;
    private Context mContext;
    private int pozicio = -1;

    public KepekAdapter(Context context, ArrayList<Kepek> kepek) {
        this.mKepekAdat = kepek;
        this.mKepekOsszesAdat = kepek;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.kepek_lista, parent, false));
    }

    @Override
    public void onBindViewHolder(KepekAdapter.ViewHolder holder, int position) {
        Kepek currentItem = mKepekAdat.get(position);
        
    holder.bindTo(currentItem);

    if(holder.getAdapterPosition() > pozicio) {
        Animation animacio = AnimationUtils.loadAnimation(mContext, R.anim.sorok_csusztatas);
        holder.itemView.startAnimation(animacio);
        pozicio = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mKepekAdat.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;
        private ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            mTextView = itemView.findViewById(R.id.hanyadikKep);
            mImageView = itemView.findViewById(R.id.itemKep);
        }
        public void bindTo(Kepek currentItem) {
            mTextView.setText(currentItem.getHanyadik());
            Glide.with(mContext).load(currentItem.getKep()).into(mImageView);
        }
    }
}