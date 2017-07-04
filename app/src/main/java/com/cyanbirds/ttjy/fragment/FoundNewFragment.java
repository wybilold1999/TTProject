package com.cyanbirds.ttjy.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.MainNewActivity;
import com.cyanbirds.ttjy.adapter.FoundNewAdapter;
import com.cyanbirds.ttjy.entity.PictureModel;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.net.request.GetDiscoverInfoRequest;
import com.cyanbirds.ttjy.net.request.GetRealUsersDiscoverInfoRequest;
import com.cyanbirds.ttjy.ui.widget.CircularProgress;
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

public class FoundNewFragment extends Fragment {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar)
    CircularProgress mProgressBar;
    Unbinder unbinder;
    private View rootView;

    private FoundNewAdapter mAdapter;
    private StaggeredGridLayoutManager layoutManager;
    private List<PictureModel> mPictureModels;
    private int pageIndex = 1;
    private int pageSize = 200;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_found, null);
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
        mToolbar.setTitle("邂逅");
        ((MainNewActivity) getActivity()).initDrawer(mToolbar);
    }

    private void setupViews() {
        layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void setupEvent(){
        mRecyclerView.addOnScrollListener(mOnScrollListener);
    }

    private void setupData() {
        mPictureModels = new ArrayList<>();
        mAdapter = new FoundNewAdapter(mPictureModels, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mProgressBar.setVisibility(View.VISIBLE);
        if("-1".equals(AppManager.getClientUser().userId) ||
                "-2".equals(AppManager.getClientUser().userId) ||
                "-3".equals(AppManager.getClientUser().userId)){
            new GetRealUsersDiscoverInfoTask().request(pageIndex,pageSize);
        } else {
            new GetDiscoverInfoTask().request(pageIndex,pageSize);
        }
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        private int lastVisibleItem;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                //获取最后一个完全显示的ItemPosition
                int[] lastVisiblePositions = manager.findLastVisibleItemPositions(new int[manager.getSpanCount()]);
                int lastVisiblePos = getMaxElem(lastVisiblePositions);
                int totalItemCount = manager.getItemCount();
                if (lastVisiblePos == (totalItemCount -1)) {
                    //加载更多
                    if("-1".equals(AppManager.getClientUser().userId)){
                        new GetRealUsersDiscoverInfoTask().request(++pageIndex,pageSize);
                    } else {
                        new GetDiscoverInfoTask().request(++pageIndex,pageSize);
                    }
                }
            }
        }
    };

    private int getMaxElem(int[] arr) {
        int size = arr.length;
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            if (arr[i]>maxVal)
                maxVal = arr[i];
        }
        return maxVal;
    }


    /**
     * 获取发现信息
     */
    class GetDiscoverInfoTask extends GetDiscoverInfoRequest {
        @Override
        public void onPostExecute(List<PictureModel> pictureModels) {
            mProgressBar.setVisibility(View.GONE);
            if(pictureModels == null || pictureModels.size() == 0){
                mAdapter.setIsShowFooter(false);
                mAdapter.notifyDataSetChanged();
                ToastUtil.showMessage(R.string.no_more_data);
            } else {
                mPictureModels.addAll(pictureModels);
                mAdapter.setIsShowFooter(true);
                mAdapter.setPictureModels(mPictureModels);
            }
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }

    /**
     * 获取真实用户的图片
     */
    class GetRealUsersDiscoverInfoTask extends GetRealUsersDiscoverInfoRequest {
        @Override
        public void onPostExecute(List<PictureModel> pictureModels) {
            mProgressBar.setVisibility(View.GONE);
            if(pictureModels == null || pictureModels.size() == 0){
                mAdapter.setIsShowFooter(false);
                mAdapter.notifyDataSetChanged();
                ToastUtil.showMessage(R.string.no_more_data);
            } else {
                mPictureModels.addAll(pictureModels);
                mAdapter.setIsShowFooter(true);
                mAdapter.setPictureModels(mPictureModels);
            }
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
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
