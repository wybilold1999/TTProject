package com.cyanbirds.ttjy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.base.BaseActivity;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.helper.SDKCoreHelper;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.utils.PreferencesUtils;
import com.umeng.analytics.MobclickAgent;
import com.yuntongxun.ecsdk.ECInitParams;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * @ClassName:EntranceActivity
 * @Description:登录和注册引导入口
 * @Author:wangyb
 * @Date:2015年5月5日下午5:26:39
 */
public class EntranceActivity extends BaseActivity {

    @BindView(R.id.login)
    FancyButton mLogin;
    @BindView(R.id.register)
    FancyButton mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
        ButterKnife.bind(this);
        saveFirstLauncher();
        setupViews();
        setEvent();
    }

    /**
     * 设置视图
     */
    private void setupViews() {
        mLogin = (FancyButton) findViewById(R.id.login);
        mRegister = (FancyButton) findViewById(R.id.register);
    }

    /**
     * 设置事件
     */
    private void setEvent() {
    }

    /**
     * 保存是否第一次启动
     */
    private void saveFirstLauncher() {
        try {
            PreferencesUtils.setIsFirstLauncher(this, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.login, R.id.register})
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.login:
                intent.setClass(this, LoginActivity.class);
                if (!TextUtils.isEmpty(AppManager.getClientUser().mobile)) {
                    intent.putExtra(ValueKey.PHONE_NUMBER, AppManager.getClientUser().mobile);
                }
                break;
            case R.id.register:
                intent.setClass(this, RegisterActivity.class);
                break;
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getName());
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }
}