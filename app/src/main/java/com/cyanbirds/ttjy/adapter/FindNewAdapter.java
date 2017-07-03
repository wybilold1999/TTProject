package com.cyanbirds.ttjy.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.entity.FindNewData;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;


/**
 * @author Cloudsoar(wangyb)
 * @datetime 2015-12-26 18:34 GMT+8
 * @email 395044952@qq.com
 */
public class FindNewAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private List<FindNewData> mFindNewDatas;
    private Context mContext;
    private boolean mShowFooter = false;

    private OnItemClickListener mOnItemClickListener;

    public FindNewAdapter(List<FindNewData> clientUsers, Context mContext) {
        this.mFindNewDatas = clientUsers;
        this.mContext = mContext;
    }

    @Override
    public int getItemViewType(int position) {
        // 最后一个item设置为footerView
        if(!mShowFooter) {
            return TYPE_ITEM;
        }
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_find_new, parent, false);
            ItemViewHolder vh = new ItemViewHolder(v);
            return vh;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.footer, null);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new FooterViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ItemViewHolder){
            FindNewData findNewData = mFindNewDatas.get(position);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            if (findNewData.mFindNewData.size() == 1) {
                itemViewHolder.mLayout1.setVisibility(View.VISIBLE);
                itemViewHolder.mLayout2.setVisibility(View.GONE);
                itemViewHolder.mLayout3.setVisibility(View.GONE);
                itemViewHolder.portrait1.setImageURI(
                        Uri.parse(findNewData.mFindNewData.get(0).face_url));
                itemViewHolder.mNickName1.setText(findNewData.mFindNewData.get(0).user_name);
                itemViewHolder.mCity1.setText(findNewData.mFindNewData.get(0).city);
            }
            if (findNewData.mFindNewData.size() == 2) {
                itemViewHolder.mLayout1.setVisibility(View.VISIBLE);
                itemViewHolder.mLayout2.setVisibility(View.VISIBLE);
                itemViewHolder.mLayout3.setVisibility(View.GONE);

                itemViewHolder.portrait1.setImageURI(
                        Uri.parse(findNewData.mFindNewData.get(0).face_url));
                itemViewHolder.mNickName1.setText(findNewData.mFindNewData.get(0).user_name);
                itemViewHolder.mCity1.setText(findNewData.mFindNewData.get(0).city);

                itemViewHolder.portrait2.setImageURI(
                        Uri.parse(findNewData.mFindNewData.get(1).face_url));
                itemViewHolder.mNickName2.setText(findNewData.mFindNewData.get(1).user_name);
                itemViewHolder.mCity2.setText(findNewData.mFindNewData.get(1).city);
            }
            if (findNewData.mFindNewData.size() == 3) {
                itemViewHolder.mLayout1.setVisibility(View.VISIBLE);
                itemViewHolder.mLayout2.setVisibility(View.VISIBLE);
                itemViewHolder.mLayout3.setVisibility(View.VISIBLE);

                itemViewHolder.portrait1.setImageURI(
                        Uri.parse(findNewData.mFindNewData.get(0).face_url));
                itemViewHolder.mNickName1.setText(findNewData.mFindNewData.get(0).user_name);
                itemViewHolder.mCity1.setText(findNewData.mFindNewData.get(0).city);

                itemViewHolder.portrait2.setImageURI(
                        Uri.parse(findNewData.mFindNewData.get(1).face_url));
                itemViewHolder.mNickName2.setText(findNewData.mFindNewData.get(1).user_name);
                itemViewHolder.mCity2.setText(findNewData.mFindNewData.get(1).city);

                itemViewHolder.portrait3.setImageURI(
                        Uri.parse(findNewData.mFindNewData.get(2).face_url));
                itemViewHolder.mNickName3.setText(findNewData.mFindNewData.get(2).user_name);
                itemViewHolder.mCity3.setText(findNewData.mFindNewData.get(2).city);
            }
        }
    }

    @Override
    public int getItemCount() {
        int begin = mShowFooter?1:0;
        if(mFindNewDatas == null) {
            return begin;
        }
        return mFindNewDatas.size() + begin;
    }

    public FindNewData getItem(int position){
        if (mFindNewDatas == null || mFindNewDatas.size() < 1) {
            return null;
        }
        return mFindNewDatas == null ? null : mFindNewDatas.get(position);
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View view) {
            super(view);
        }

    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        SimpleDraweeView portrait1;
        SimpleDraweeView portrait2;
        SimpleDraweeView portrait3;
        LinearLayout mLayout1;
        LinearLayout mLayout2;
        LinearLayout mLayout3;
        TextView mNickName1;
        TextView mNickName2;
        TextView mNickName3;
        TextView mCity1;
        TextView mCity2;
        TextView mCity3;
        public ItemViewHolder(View itemView) {
            super(itemView);
            portrait1 = (SimpleDraweeView) itemView.findViewById(R.id.portrait_1);
            portrait2 = (SimpleDraweeView) itemView.findViewById(R.id.portrait_2);
            portrait3 = (SimpleDraweeView) itemView.findViewById(R.id.portrait_3);
            mLayout1 = (LinearLayout) itemView.findViewById(R.id.lay_1);
            mLayout2 = (LinearLayout) itemView.findViewById(R.id.lay_2);
            mLayout3 = (LinearLayout) itemView.findViewById(R.id.lay_3);
            mNickName1 = (TextView) itemView.findViewById(R.id.nickname_1);
            mNickName2 = (TextView) itemView.findViewById(R.id.nickname_2);
            mNickName3 = (TextView) itemView.findViewById(R.id.nickname_3);
            mCity1 = (TextView) itemView.findViewById(R.id.city_1);
            mCity2 = (TextView) itemView.findViewById(R.id.city_2);
            mCity3 = (TextView) itemView.findViewById(R.id.city_3);
            portrait1.setOnClickListener(this);
            portrait2.setOnClickListener(this);
            portrait3.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int index = 0;
            switch (v.getId()) {
                case R.id.portrait_1:
                    index = 0;
                break;
                case R.id.portrait_2:
                    index = 1;
                    break;
                case R.id.portrait_3:
                    index = 2;
                    break;
            }
            if(mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, getAdapterPosition(), index);
            }
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position, int index);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setIsShowFooter(boolean showFooter) {
        this.mShowFooter = showFooter;
    }

    public boolean isShowFooter() {
        return this.mShowFooter;
    }

    public void setClientUsers(List<FindNewData> users){
        this.mFindNewDatas = users;
        this.notifyDataSetChanged();
    }
}
