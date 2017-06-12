package com.cyanbirds.ttjy.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.base.BaseActivity;
import com.cyanbirds.ttjy.utils.ToastUtil;
import com.cyanbirds.ttjy.utils.VibratorUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wangyb on 2017/6/12.
 * 描述：拨打电话
 */

public class VoipCallActivity extends BaseActivity {

    @BindView(R.id.portrait)
    SimpleDraweeView mPortrait;
    @BindView(R.id.nick_name)
    TextView mNickName;
    @BindView(R.id.answer)
    ImageView mAnswer;
    @BindView(R.id.decline)
    ImageView mDecline;

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voip_call);
        ButterKnife.bind(this);
        setupData();
    }

    private void setupData() {
        timer = new CountDownTimer(50000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                timer.cancel();
                finish();
            }

        };
        timer.start();
        VibratorUtil.start();
    }

    @OnClick({R.id.answer, R.id.decline})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.answer:
                break;
            case R.id.decline:
                ToastUtil.showMessage(R.string.decline_call);
                VibratorUtil.cancel();
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VibratorUtil.cancel();
    }
}
