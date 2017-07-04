package com.cyanbirds.ttjy.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.MainNewActivity;
import com.cyanbirds.ttjy.adapter.CardAdapter;
import com.cyanbirds.ttjy.entity.CardModel;
import com.cyanbirds.ttjy.entity.YuanFenModel;
import com.cyanbirds.ttjy.net.request.GetYuanFenUserRequest;
import com.cyanbirds.ttjy.net.request.SendGreetRequest;
import com.cyanbirds.ttjy.utils.ToastUtil;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by wangyb on 2017/6/29.
 * 描述：
 */

public class CardFragment extends Fragment implements SwipeFlingAdapterView.onFlingListener,
        SwipeFlingAdapterView.OnItemClickListener {


    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.left)
    ImageView mLeft;
    @BindView(R.id.info)
    ImageView mInfo;
    @BindView(R.id.right)
    ImageView mRight;
    @BindView(R.id.frame)
    SwipeFlingAdapterView mFrame;
    private View rootView;
    private Unbinder unbinder;

    private List<CardModel> dataList;
    private CardAdapter mAdapter;
    private int pageNo = 1;
    private int pageSize = 200;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_card, null);
            unbinder = ButterKnife.bind(this, rootView);
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
        mToolbar.setTitle("缘分");
        ((MainNewActivity) getActivity()).initDrawer(mToolbar);
    }

    private void setupEvent() {
        mFrame.setOnItemClickListener(this);
        mFrame.setFlingListener(this);
    }

    private void setupData() {
        dataList = new ArrayList<>();
        mAdapter = new CardAdapter(getActivity(), dataList);
        mFrame.setAdapter(mAdapter);

        new GetYuanFenUserTask().request(pageNo, pageSize);
    }

    @OnClick({R.id.left, R.id.info, R.id.right})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.left:
                mFrame.getTopCardListener().selectLeft();
                break;
            case R.id.info:
                break;
            case R.id.right:
                mFrame.getTopCardListener().selectRight();
                break;
        }
    }

    @Override
    public void removeFirstObjectInAdapter() {
        dataList.remove(0);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLeftCardExit(Object dataObject) {

    }

    @Override
    public void onRightCardExit(Object dataObject) {

    }

    @Override
    public void onAdapterAboutToEmpty(int itemsInAdapter) {

    }

    @Override
    public void onScroll(float scrollProgressPercent) {
        try {
            View view = mFrame.getSelectedView();
            view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
            view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClicked(int itemPosition, Object dataObject) {

    }

    class GetYuanFenUserTask extends GetYuanFenUserRequest {
        @Override
        public void onPostExecute(List<YuanFenModel> yuanFenModels) {
            if (yuanFenModels != null && yuanFenModels.size() > 0) {
                for (YuanFenModel model : yuanFenModels) {
                    CardModel dataItem = new CardModel();
                    dataItem.userId = model.uid;
                    dataItem.userName = model.nickname;
                    dataItem.imagePath = model.faceUrl;
                    dataItem.city = model.city;
                    dataItem.age = model.age;
                    dataItem.constellation = model.constellation;
                    dataItem.distance = model.distance == null ? 0.00 : model.distance;
                    dataItem.signature = model.signature;
                    dataItem.pictures = model.pictures;
                    dataList.add(dataItem);
                }
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onErrorExecute(String error) {
            ToastUtil.showMessage(error);
        }
    }

    class SenderGreetTask extends SendGreetRequest {
        @Override
        public void onPostExecute(String s) {
            ToastUtil.showMessage(s);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

}
