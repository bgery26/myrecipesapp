package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.contentcapture.ContentCaptureCondition;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecipeItemAdapter extends RecyclerView.Adapter<RecipeItemAdapter.ViewHolder> implements Filterable {
    private ArrayList<RecipeItem> mRecipeItemsData;
    private ArrayList<RecipeItem> mRecipeItemsDataAll;
    private Context mContext;
    private int lastPosition = -1;

    RecipeItemAdapter(Context context, ArrayList<RecipeItem> itemsData) {
        this.mRecipeItemsData = itemsData;
        this.mRecipeItemsDataAll = itemsData;
        this.mContext = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(RecipeItemAdapter.ViewHolder holder, int position) {
        RecipeItem currentItem = mRecipeItemsData.get(position);

        holder.bindTo(currentItem);

        if(holder.getAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }

    }

    @Override
    public int getItemCount() {return mRecipeItemsData.size();}

    @Override
    public Filter getFilter() {return recipeFilter;}

    private Filter recipeFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            ArrayList<RecipeItem> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();
            if(charSequence == null || charSequence.length() == 0) {
                results.count = mRecipeItemsDataAll.size();
                results.values = mRecipeItemsDataAll;

            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for(RecipeItem item : mRecipeItemsDataAll) {
                    if(item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;

            }


            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mRecipeItemsData = (ArrayList) filterResults.values;
            notifyDataSetChanged();

        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitleText;
        private TextView minfoText;
        private TextView mTimeText;
        private ImageView mItemImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitleText = itemView.findViewById(R.id.itemTitle);
            minfoText = itemView.findViewById(R.id.subTitle);
            mTimeText = itemView.findViewById(R.id.cooktime);
            mItemImage = itemView.findViewById(R.id.itemImage);

        }

        public void bindTo(RecipeItem currentItem) {
            mTitleText.setText(currentItem.getName());
            minfoText.setText(currentItem.getInfo());
            mTimeText.setText(currentItem.getTime());

            Glide.with(mContext).load(currentItem.getImageResource()).into(mItemImage);
            itemView.findViewById(R.id.delete).setOnClickListener(view -> ((RecipeListActivity)mContext).deleteItem(currentItem));

        }
    }
}


