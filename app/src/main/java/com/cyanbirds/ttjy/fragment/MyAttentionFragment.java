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
import com.cyanbirds.ttjy.adapter.MyAttentionAdapter;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.entity.FollowModel;
import com.cyanbirds.ttjy.net.request.FollowListRequest;
import com.cyanbirds.ttjy.ui.widget.CircularProgress;
import com.cyanbirds.ttjy.ui.widget.DividerItemDecoration;
import com.cyanbirds.ttjy.ui.widget.WrapperLinearLayoutManager;
import com.cyanbirds.ttjy.utils.DensityUtil;
import com.cyanbirds.ttjy.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

/**
 * Created by wangyb on 2017/6/29.
 * 描述：
 */

public class MyAttentionFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private CircularProgress mCircularProgress;
    private TextView mNoUserInfo;
    private MyAttentionAdapter mAdapter;
    private View rootView;

    private int pageNo = 1;
    private int pageSize = 200;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_my_attention, null);
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
        mNoUserInfo = (TextView) rootView.findViewById(R.id.info);
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
        mAdapter = new MyAttentionAdapter(getActivity());
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mCircularProgress.setVisibility(View.VISIBLE);
        new FollowListTask().request("followList", pageNo, pageSize);
    }

    private MyAttentionAdapter.OnItemClickListener mOnItemClickListener = new MyAttentionAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            FollowModel followModel = mAdapter.getItem(position);
            Intent intent = new Intent(getActivity(), PersonalInfoActivity.class);
            intent.putExtra(ValueKey.USER_ID, String.valueOf(followModel.userId));
            startActivity(intent);
        }
    };

    class FollowListTask extends FollowListRequest {
        @Override
        public void onPostExecute(List<FollowModel> followModels) {
            mCircularProgress.setVisibility(View.GONE);
            if(null != followModels && followModels.size() > 0){
                mNoUserInfo.setVisibility(View.GONE);
                mAdapter.setFollowModels(followModels);
            } else {
                mNoUserInfo.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onErrorExecute(String error) {
            ToastUtil.showMessage(error);
            mCircularProgress.setVisibility(View.GONE);
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
