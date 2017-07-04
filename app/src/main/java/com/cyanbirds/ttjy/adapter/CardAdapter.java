package com.cyanbirds.ttjy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.entity.CardModel;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

/**
 * Created by Shall on 2015-06-23.
 */
public class CardAdapter extends BaseAdapter {
    private Context mContext;
    private List<CardModel> mCardList;

    public CardAdapter(Context mContext, List<CardModel> mCardList) {
        this.mContext = mContext;
        this.mCardList = mCardList;
    }

    @Override
    public int getCount() {
        return mCardList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCardList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.card_item, parent, false);
            holder = new ViewHolder();
            holder.mPortrait = (SimpleDraweeView) convertView.findViewById(R.id.card_image_portrait);
            holder.mImgOne = (SimpleDraweeView) convertView.findViewById(R.id.card_image_one);
            holder.mImgTwo = (SimpleDraweeView) convertView.findViewById(R.id.card_image_two);
            holder.mImgThree = (SimpleDraweeView) convertView.findViewById(R.id.card_image_three);
            holder.mImgFour = (SimpleDraweeView) convertView.findViewById(R.id.card_image_four);
            holder.mNickName = (TextView) convertView.findViewById(R.id.card_user_name);
            holder.mAge = (TextView) convertView.findViewById(R.id.age);
            holder.mCon = (TextView) convertView.findViewById(R.id.constellation);
            holder.mSignature = (TextView) convertView.findViewById(R.id.signature);
            holder.mDistance = (TextView) convertView.findViewById(R.id.distance);
            holder.mCity = (TextView) convertView.findViewById(R.id.city);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    class ViewHolder {
        SimpleDraweeView mPortrait;
        SimpleDraweeView mImgOne;
        SimpleDraweeView mImgTwo;
        SimpleDraweeView mImgThree;
        SimpleDraweeView mImgFour;
        TextView mNickName;
        TextView mAge;
        TextView mCon;
        TextView mSignature;
        TextView mDistance;
        TextView mCity;
    }
}
