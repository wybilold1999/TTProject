package com.cyanbirds.ttjy.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.MainNewActivity;
import com.cyanbirds.ttjy.activity.PersonalInfoActivity;
import com.cyanbirds.ttjy.adapter.MyGiftsAdapter;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.entity.ReceiveGiftModel;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.net.request.GiftsListRequest;
import com.cyanbirds.ttjy.ui.widget.CircularProgress;
import com.cyanbirds.ttjy.ui.widget.DividerItemDecoration;
import com.cyanbirds.ttjy.ui.widget.WrapperLinearLayoutManager;
import com.cyanbirds.ttjy.utils.DensityUtil;
import com.cyanbirds.ttjy.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyb on 2017/6/29.
 * 描述：
 */

public class GiftsFragment extends Fragment {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private CircularProgress mCircularProgress;
    private TextView mNoUserInfo;
    private MyGiftsAdapter mAdapter;
    private View rootView;

    private int pageNo = 1;
    private int pageSize = 13;
    private List<ReceiveGiftModel> mReceiveGiftModels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_gifts, null);
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainNewActivity) getActivity()).initDrawer(mToolbar);
    }

    private void setupView(){
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
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
        if (AppManager.getClientUser().isShowVip) {
            if (AppManager.getClientUser().is_vip) {
                pageSize = 200;
            }
        } else {
            pageSize = 200;
        }
        mReceiveGiftModels = new ArrayList<>();
        mAdapter = new MyGiftsAdapter(getActivity());
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mCircularProgress.setVisibility(View.VISIBLE);
        new MyGiftListTask().request(pageNo, pageSize);
    }

    private MyGiftsAdapter.OnItemClickListener mOnItemClickListener = new MyGiftsAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            ReceiveGiftModel receiveGiftModel = mAdapter.getItem(position);
            Intent intent = new Intent(getActivity(), PersonalInfoActivity.class);
            intent.putExtra(ValueKey.USER_ID, String.valueOf(receiveGiftModel.userId));
            startActivity(intent);
        }
    };

    class MyGiftListTask extends GiftsListRequest {
        @Override
        public void onPostExecute(List<ReceiveGiftModel> receiveGiftModels) {
            mCircularProgress.setVisibility(View.GONE);
            if(null != receiveGiftModels && receiveGiftModels.size() > 0){
                if (AppManager.getClientUser().isShowVip &&
                        !AppManager.getClientUser().is_vip &&
                        receiveGiftModels.size() > 10) {//如果不是vip，移除前面3个
                    mAdapter.setIsShowFooter(true);
                    List<String> urls = new ArrayList<>(3);
                    urls.add(receiveGiftModels.get(0).faceUrl);
                    urls.add(receiveGiftModels.get(1).faceUrl);
                    urls.add(receiveGiftModels.get(2).faceUrl);
                    mAdapter.setFooterFaceUrls(urls);
                    receiveGiftModels.remove(0);
                    receiveGiftModels.remove(1);
                }
                mCircularProgress.setVisibility(View.GONE);
                mReceiveGiftModels.addAll(receiveGiftModels);
                mAdapter.setReceiveGiftModel(mReceiveGiftModels);
            } else {
                if (receiveGiftModels != null) {
                    mReceiveGiftModels.addAll(receiveGiftModels);
                }
                mAdapter.setIsShowFooter(false);
                mAdapter.setReceiveGiftModel(mReceiveGiftModels);
            }
            if (mReceiveGiftModels != null && mReceiveGiftModels.size() > 0) {
                mNoUserInfo.setVisibility(View.GONE);
            } else {
                mNoUserInfo.setText("您还没有收到礼物哦");
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
