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
import com.cyanbirds.ttjy.adapter.LoveFormeAdapter;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.entity.LoveModel;
import com.cyanbirds.ttjy.net.request.GetLoveFormeListRequest;
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

public class LoveMeFragment extends Fragment {

    private RecyclerView mRecyclerView;
    TextView mNoUserinfo;
    private CircularProgress mCircularProgress;
    private View rootView;

    private LoveFormeAdapter mAdapter;
    private List<LoveModel> mLoveModels;

    private int pageNo = 1;
    private int pageSize = 200;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_love_me, null);
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

    private void setupView() {
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


    private void setupData() {
        mAdapter = new LoveFormeAdapter(getActivity());
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mCircularProgress.setVisibility(View.VISIBLE);
        new GetLoveFormeListTask().request(pageNo, pageSize);
    }

    private LoveFormeAdapter.OnItemClickListener mOnItemClickListener = new LoveFormeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            LoveModel loveModel = mLoveModels.get(position);
            Intent intent = new Intent(getActivity(), PersonalInfoNewActivity.class);
            intent.putExtra(ValueKey.USER_ID, String.valueOf(loveModel.userId));
            startActivity(intent);
        }
    };

    class GetLoveFormeListTask extends GetLoveFormeListRequest {
        @Override
        public void onPostExecute(List<LoveModel> loveModels) {
            mCircularProgress.setVisibility(View.GONE);
            if(loveModels != null && loveModels.size() > 0){
                mNoUserinfo.setVisibility(View.GONE);
                mLoveModels = loveModels;
                mAdapter.setLoveModels(loveModels);
            } else {
                mNoUserinfo.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onErrorExecute(String error) {
            mCircularProgress.setVisibility(View.GONE);
            ToastUtil.showMessage(error);
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
