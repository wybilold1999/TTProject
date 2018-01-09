package com.cyanbirds.ttjy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.base.BaseActivity;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wangyb on 2018/1/9.
 */

public class AppointmentActivity extends BaseActivity {

    @BindView(R.id.toolbar_actionbar)
    Toolbar mToolbarActionbar;
    @BindView(R.id.appointment_prj)
    TextView mAppointmentPrj;
    @BindView(R.id.appointment_prj_lay)
    RelativeLayout mAppointmentPrjLay;
    @BindView(R.id.appointment_time)
    TextView mAppointmentTime;
    @BindView(R.id.appointment_info_lay)
    RelativeLayout mAppointmentInfoLay;
    @BindView(R.id.appointment_long)
    TextView mAppointmentLong;
    @BindView(R.id.appointment_long_lay)
    RelativeLayout mAppointmentLongLay;
    @BindView(R.id.appointment_address)
    TextView mAppointmentAddress;
    @BindView(R.id.appointment_address_lay)
    RelativeLayout mAppointmentAddressLay;
    @BindView(R.id.appointment_remark)
    EditText mAppointmentRemark;
    @BindView(R.id.appointment_remark_lay)
    RelativeLayout mAppointmentRemarkLay;

    /**
     * 分享位置
     */
    public final static int SHARE_LOCATION_RESULT = 106;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);
        ButterKnife.bind(this);
        mToolbarActionbar.setNavigationIcon(R.mipmap.ic_up);
    }

    @OnClick({R.id.appointment_prj_lay, R.id.appointment_long_lay, R.id.appointment_address_lay, R.id.appointment_remark_lay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.appointment_prj_lay:
                showPrjDialog();
                break;
            case R.id.appointment_long_lay:
                showTimeLongDialog();
                break;
            case R.id.appointment_address_lay:
                toShareLocation();
                break;
            case R.id.appointment_remark_lay:
                if (mAppointmentRemark.requestFocus()) {
                    showSoftKeyboard(view);
                }
                break;
        }
    }

    /**
     * 显示约会项目
     */
    private void showPrjDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tv_project);
        final String[] array = getResources().getStringArray(R.array.appointment_content);
        builder.setItems(R.array.appointment_content, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                mAppointmentPrj.setText(array[i]);
            }
        });
        builder.show();
    }

    /**
     * 显示约会时长
     */
    private void showTimeLongDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tv_appointment_time);
        final String[] array = getResources().getStringArray(R.array.appointment_long);
        builder.setItems(R.array.appointment_long, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                mAppointmentLong.setText(array[i]);
            }
        });
        builder.show();
    }

    /**
     * 跳到分享位置界面
     */
    private void toShareLocation() {
        Intent intent = new Intent(this, ShareLocationActivity.class);
        intent.putExtra(ValueKey.FROM_ACTIVITY, this.getClass().getSimpleName());
        startActivityForResult(intent, SHARE_LOCATION_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK
                && requestCode == SHARE_LOCATION_RESULT) {
            String address = data.getStringExtra(ValueKey.ADDRESS);
            if (!TextUtils.isEmpty(address)) {
                mAppointmentAddress.setText(address);
            }
        }
    }


}
