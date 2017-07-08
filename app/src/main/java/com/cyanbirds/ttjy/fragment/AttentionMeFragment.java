package com.cyanbirds.ttjy.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.PersonalInfoActivity;
import com.cyanbirds.ttjy.activity.PersonalInfoNewActivity;
import com.cyanbirds.ttjy.adapter.AttentionMeAdapter;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.entity.FollowModel;
import com.cyanbirds.ttjy.net.request.FollowListRequest;
import com.cyanbirds.ttjy.ui.widget.CircularProgress;
import com.cyanbirds.ttjy.ui.widget.DividerItemDecoration;
import com.cyanbirds.ttjy.ui.widget.WrapperLinearLayoutManager;
import com.cyanbirds.ttjy.utils.DensityUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyb on 2017/6/29.
 * 描述：
 */

public class AttentionMeFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private CircularProgress mCircularProgress;
    private TextView mNoUserinfo;
    private View rootView;

    private AttentionMeAdapter mAdapter;
    private List<FollowModel> mFollowModels;
    private int pageNo = 1;
    private int pageSize = 200;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_attention_me, null);
            setupView();
            setupData();
            setHasOptionsMenu(true);

        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    private void setupView(){
        mCircularProgress = (CircularProgress) rootView.findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        mNoUserinfo = (TextView) rootView.findViewById(R.id.info);
        LinearLayoutManager manager = new WrapperLinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayout.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL, DensityUtil
                .dip2px(getActivity(), 12), DensityUtil.dip2px(
                getActivity(), 12)));
    }


    private void setupData(){
        mFollowModels = new ArrayList<>();
        mAdapter = new AttentionMeAdapter(getActivity());
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mCircularProgress.setVisibility(View.VISIBLE);
        new FollowListTask().request("followFormeList", pageNo, pageSize);
    }

    private AttentionMeAdapter.OnItemClickListener mOnItemClickListener = new AttentionMeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            FollowModel followModel = mFollowModels.get(position);
            Intent intent = new Intent(getActivity(), PersonalInfoNewActivity.class);
            intent.putExtra(ValueKey.USER_ID, String.valueOf(followModel.userId));
            startActivity(intent);
        }
    };

    class FollowListTask extends FollowListRequest {
        @Override
        public void onPostExecute(List<FollowModel> followModels) {
            mCircularProgress.setVisibility(View.GONE);
            if(followModels != null && followModels.size() > 0){
                mFollowModels.addAll(followModels);
                mAdapter.setIsShowFooter(false);
                mAdapter.setFollowModels(mFollowModels);
            }
            if (mFollowModels != null && mFollowModels.size() > 0) {
                mNoUserinfo.setVisibility(View.GONE);
            } else {
                mNoUserinfo.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getName());
    }
}
