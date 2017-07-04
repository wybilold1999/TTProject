package com.cyanbirds.ttjy.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.MainNewActivity;
import com.cyanbirds.ttjy.activity.PersonalInfoActivity;
import com.cyanbirds.ttjy.adapter.FindNewAdapter;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.entity.FindNewData;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.net.request.GetFindNewRequest;
import com.cyanbirds.ttjy.ui.widget.CircularProgress;
import com.cyanbirds.ttjy.ui.widget.WrapperLinearLayoutManager;
import com.cyanbirds.ttjy.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by wangyb on 2017/6/29.
 * 描述：
 */

public class FindNewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener  {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.progress_bar)
    CircularProgress mProgress;
    Unbinder unbinder;
    private View rootView;

    private LinearLayoutManager layoutManager;
    private List<FindNewData> mClientUsers;
    private FindNewAdapter mAdapter;
    private final long freshSpan = 3 * 60 * 1000;//3分钟之后才运行下拉刷新
    private long freshTime = 0;
    private int pageIndex = 1;
    private int pageSize = 200;
    private String GENDER = ""; //空表示查询和自己性别相反的用户
    /**
     * 0:同城 1：缘分 2：颜值  -1:就是全国
     */
    private String mUserScopeType = "2";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_find_love, null);
            unbinder = ButterKnife.bind(this, rootView);
            setupViews();
            setupEvent();
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
        mToolbar.setTitle("颜值");
        ((MainNewActivity) getActivity()).initDrawer(mToolbar);
    }

    private void setupViews() {
        mSwipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        layoutManager = new WrapperLinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void setupEvent() {
        mSwipeRefresh.setOnRefreshListener(this);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
    }

    private void setupData() {
        mClientUsers = new ArrayList<>();
        mAdapter = new FindNewAdapter(mClientUsers, getActivity());
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mProgress.setVisibility(View.VISIBLE);
        if("男".equals(AppManager.getClientUser().sex)){
            GENDER = "FeMale";
        } else {
            GENDER = "Male";
        }
        if("-1".equals(AppManager.getClientUser().userId) ||
                "-2".equals(AppManager.getClientUser().userId) ||
                "-3".equals(AppManager.getClientUser().userId)){ //客服登陆，获取真实用户
        } else {
            new GetFindLoveTask().request(pageIndex, pageSize, GENDER, mUserScopeType);
        }
    }

    @Override
    public void onRefresh() {
        mSwipeRefresh.setRefreshing(true);
        if (System.currentTimeMillis() > freshTime + freshSpan) {
            getFreshData();
        } else {
            mSwipeRefresh.setRefreshing(false);
        }
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        private int lastVisibleItem;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            lastVisibleItem = layoutManager.findLastVisibleItemPosition();
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && lastVisibleItem + 1 == mAdapter.getItemCount()
                    && mAdapter.isShowFooter()) {
                //加载更多
                //请求数据
                if("-1".equals(AppManager.getClientUser().userId) ||
                        "-2".equals(AppManager.getClientUser().userId) ||
                        "-3".equals(AppManager.getClientUser().userId)){ //客服登陆，获取真实用户
                } else {
                    mUserScopeType = "1";
                    new GetFindLoveTask().request(pageIndex, pageSize, GENDER, mUserScopeType);
                    pageIndex++;
                }
            }
        }
    };

    private FindNewAdapter.OnItemClickListener mOnItemClickListener = new FindNewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position, int index) {
            FindNewData findNewData = mAdapter.getItem(position);
            if (findNewData != null) {
                Intent intent = new Intent(getActivity(), PersonalInfoActivity.class);
                intent.putExtra(ValueKey.USER_ID, findNewData.mFindNewData.get(index).userId);
                intent.putExtra(ValueKey.FROM_ACTIVITY, "FindLoveFragment");
                startActivity(intent);
            }
        }
    };

    class GetFindLoveTask extends GetFindNewRequest {
        @Override
        public void onPostExecute(List<FindNewData> userList) {

            freshTime = System.currentTimeMillis();
            mProgress.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
            if (pageIndex == 1) {//进行筛选的时候，滑动到顶部
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
            if(userList == null || userList.size() == 0){
                mAdapter.setIsShowFooter(false);
                mAdapter.notifyDataSetChanged();
                ToastUtil.showMessage(R.string.no_more_data);
            } else {
                mClientUsers.addAll(userList);
                mAdapter.setIsShowFooter(true);
                mAdapter.setClientUsers(mClientUsers);
            }
        }

        @Override
        public void onErrorExecute(String error) {
            ToastUtil.showMessage(error);
            mProgress.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
            mAdapter.setIsShowFooter(false);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void getFreshData() {
        if("-1".equals(AppManager.getClientUser().userId) ||
                "-2".equals(AppManager.getClientUser().userId) ||
                "-3".equals(AppManager.getClientUser().userId)){ //客服登陆，获取真实用户
        } else {
            new GetFreshFindLoveTask().request(++pageIndex, pageSize, GENDER, mUserScopeType);
        }
    }

    class GetFreshFindLoveTask extends GetFindNewRequest{
        @Override
        public void onPostExecute(List<FindNewData> userList) {
            mProgress.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
            if(userList == null || userList.size() == 0){//没有数据了就又从第一页开始查找
                pageIndex = 1;
            } else {
                freshTime = System.currentTimeMillis();
                mClientUsers.clear();
                mClientUsers.addAll(userList);
                mAdapter.setIsShowFooter(false);
                mAdapter.setClientUsers(mClientUsers);
            }
        }

        @Override
        public void onErrorExecute(String error) {
            mProgress.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mSwipeRefresh) {
            mSwipeRefresh.setOnRefreshListener(null);
        }
        unbinder.unbind();
    }
}
