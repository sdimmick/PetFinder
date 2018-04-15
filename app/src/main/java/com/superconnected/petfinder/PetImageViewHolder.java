package com.superconnected.petfinder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.superconnected.petfinder.models.PetImage;
import com.yahoo.squidb.recyclerview.SquidViewHolder;

public class PetImageViewHolder extends SquidViewHolder<PetImage> {
    private ImageView mPetImageView;

    public PetImageViewHolder(View itemView) {
        super(itemView, new PetImage());
        mPetImageView = itemView.findViewById(R.id.pet_image);
    }

    public void setImage(String path, Context context) {
        GlideApp
                .with(context)
                .load(path)
                .into(mPetImageView);
    }
}
