package com.superconnected.petfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.superconnected.petfinder.models.PetImage;
import com.yahoo.squidb.recyclerview.SquidRecyclerAdapter;

public class PetListAdapter extends SquidRecyclerAdapter<PetImage, PetImageViewHolder> {
    private Context mContext;

    public PetListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public PetImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pet_list_item, parent, false);

        return new PetImageViewHolder(view);
    }

    @Override
    public void onBindSquidViewHolder(PetImageViewHolder holder, int position) {
        PetImage petImage = holder.item;
        holder.setImage(petImage.getPath(), mContext);
    }

}
