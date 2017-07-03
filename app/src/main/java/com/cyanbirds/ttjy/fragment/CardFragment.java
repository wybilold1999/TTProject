package com.cyanbirds.ttjy.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.MainNewActivity;
import com.cyanbirds.ttjy.activity.PersonalInfoActivity;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.entity.YuanFenModel;
import com.cyanbirds.ttjy.net.request.AddLoveRequest;
import com.cyanbirds.ttjy.net.request.GetYuanFenUserRequest;
import com.cyanbirds.ttjy.net.request.SendGreetRequest;
import com.cyanbirds.ttjy.utils.ToastUtil;
import com.stone.card.CardDataItem;
import com.stone.card.CardSlidePanel;
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

public class CardFragment extends Fragment implements CardSlidePanel.CardSwitchListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.card_left_btn)
    Button mCardLeftBtn;
    @BindView(R.id.personal_btn)
    Button mPersonalBtn;
    @BindView(R.id.card_right_btn)
    Button mCardRightBtn;
    @BindView(R.id.card_bottom_layout)
    LinearLayout mCardBottomLayout;
    @BindView(R.id.image_slide_panel)
    CardSlidePanel mImageSlidePanel;
    /*@BindView(R.id.data_lay)
    LinearLayout mDataLay;
    @BindView(R.id.radar_img)
    ImageView mRadarImg;
    @BindView(R.id.radar_bttom_img)
    ImageView mRadarBttomImg;
    @BindView(R.id.radar_top_img)
    ImageView mRadarTopImg;
    @BindView(R.id.portrait)
    SimpleDraweeView mPortrait;
    @BindView(R.id.loading_lay)
    RelativeLayout mLoadingLay;*/

    private View rootView;
    private Unbinder unbinder;
    private AnimationSet grayAnimal;

    private List<CardDataItem> dataList = new ArrayList<>();
    private List<YuanFenModel> models;
    private int curUserId;

    private int pageNo = 0;
    private int pageSize = 200;
    private Handler mHandler = new Handler();

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
        ((MainNewActivity) getActivity()).initDrawer(mToolbar);
    }

    private void setupEvent() {
        mImageSlidePanel.setCardSwitchListener(this);
    }

    private void setupData() {
//        mLoadingLay.setVisibility(View.GONE);
//        mDataLay.setVisibility(View.VISIBLE);
        /*if (!TextUtils.isEmpty(AppManager.getClientUser().face_local)) {
            mPortrait.setImageURI(Uri.parse("file://" + AppManager.getClientUser().face_local));
        }
        startcircularAnima();*/

        CardDataItem dataItem = null;
        models = (List<YuanFenModel>) getArguments().getSerializable(ValueKey.USER);
        for (YuanFenModel model : models) {
            dataItem = new CardDataItem();
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
        mImageSlidePanel.fillData(dataList);
        mImageSlidePanel.invalidate();

       /* mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new GetYuanFenUserTask().request(pageNo, pageSize);
            }
        }, 3000);*/
    }


    @Override
    public void onShow(int index) {
        curUserId = dataList.get(index).userId;
        if (index >= dataList.size() - 3) {
            //请求数据
            new GetYuanFenUserTask().request(pageNo, pageSize);
        }
    }

    @Override
    public void onCardVanish(int index, int type) {
        if (type == 1) {//向右滑
            new SenderGreetTask().request(String.valueOf(curUserId));
            new AddLoveRequest().request(String.valueOf(curUserId));
        }
    }

    @Override
    public void onItemClick(View cardImageView, int index) {
        Intent intent = new Intent(getActivity(), PersonalInfoActivity.class);
        intent.putExtra(ValueKey.USER_ID, String.valueOf(dataList.get(index).userId));
        startActivity(intent);
    }

    @OnClick({R.id.personal_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.personal_btn:
                Intent intent = new Intent(getActivity(), PersonalInfoActivity.class);
                intent.putExtra(ValueKey.USER_ID, String.valueOf(curUserId));
                startActivity(intent);
                break;
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

    class GetYuanFenUserTask extends GetYuanFenUserRequest {
        @Override
        public void onPostExecute(List<YuanFenModel> yuanFenModels) {
            if (yuanFenModels != null && yuanFenModels.size() > 0) {
                for (YuanFenModel model : yuanFenModels) {
                    CardDataItem dataItem = new CardDataItem();
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
                mImageSlidePanel.fillData(dataList);
            }
        }

        @Override
        public void onErrorExecute(String error) {
            ToastUtil.showMessage(error);
        }
    }

    /*private void startcircularAnima() {
        grayAnimal = playHeartbeatAnimation();
        mRadarBttomImg.startAnimation(grayAnimal);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startwhiteAnimal();
            }
        }, 400);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startannularAnimat();
            }
        }, 600);
    }

    private AnimationSet playHeartbeatAnimation() {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation sa = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(900);
        sa.setFillAfter(true);
        sa.setRepeatCount(0);
        sa.setInterpolator(new LinearInterpolator());
        animationSet.addAnimation(sa);
        return animationSet;
    }

    private void startannularAnimat() {
        mRadarImg.setVisibility(View.VISIBLE);
        AnimationSet annularAnimat = getAnimAnnular();
        annularAnimat.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRadarImg.setVisibility(View.GONE);
            }
        });
        mRadarImg.startAnimation(annularAnimat);
    }

    private void startwhiteAnimal() {
        AnimationSet whiteAnimal = playHeartbeatAnimation();
        whiteAnimal.setRepeatCount(0);
        whiteAnimal.setDuration(700);
        mRadarTopImg.setVisibility(View.VISIBLE);
        mRadarTopImg.startAnimation(whiteAnimal);
        whiteAnimal.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRadarImg.setVisibility(View.GONE);
                mRadarTopImg.setVisibility(View.GONE);
                startcircularAnima();
            }
        });

    }

    private AnimationSet getAnimAnnular() {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation sa = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        animationSet.addAnimation(new AlphaAnimation(1.0f, 0.1f));
        animationSet.setDuration(400);
        sa.setDuration(500);
        sa.setFillAfter(true);
        sa.setRepeatCount(0);
        sa.setInterpolator(new LinearInterpolator());
        animationSet.addAnimation(sa);
        return animationSet;
    }*/

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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
