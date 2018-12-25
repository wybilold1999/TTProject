package com.cyanbirds.ttjy.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.base.BaseActivity;
import com.cyanbirds.ttjy.manager.AppManager;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wangyb on 2017/7/21.
 * 描述：客服
 */

public class CustomServiceActivity extends BaseActivity {

    @BindView(R.id.tv_vip)
    TextView mVipTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_service);
        ButterKnife.bind(this);
        Toolbar toolbar = getActionBarToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_up);
        }
        setupView();
    }

    private void setupView() {
        if(AppManager.getClientUser().isShowVip) {
            mVipTv.setVisibility(View.VISIBLE);
            String exchange = getResources().getString(R.string.my_custom_service_vip);
            mVipTv.setText(Html.fromHtml(exchange));
        } else {
            mVipTv.setVisibility(View.GONE);
        }
    }
}
