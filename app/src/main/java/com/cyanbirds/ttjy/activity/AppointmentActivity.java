package com.cyanbirds.ttjy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.base.BaseActivity;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.net.request.ApplyForAppointmentRequest;
import com.cyanbirds.ttjy.utils.DateUtil;
import com.cyanbirds.ttjy.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    @BindView(R.id.sure)
    TextView mSure;

    private String mApplyForUid;//向谁申请
    private List<String> mDateList;
    private List<String> mTimeList;
    private View mDateTimeView;

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

        mApplyForUid = getIntent().getStringExtra(ValueKey.USER_ID);
        initData();
    }

    private void initData() {
        mDateList = new ArrayList<>();
        mTimeList = new ArrayList<>();

        /**
         * 日期
         */
        Calendar c = Calendar.getInstance();
        for (int i = 1; i < 7; i++) {
            c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 1);
            mDateList.add(DateUtil.toDateTime_2(c.getTimeInMillis()));
        }

        /**
         * 时间
         */
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < 48; i++) {
            mTimeList.add(DateUtil.toTimeM(cal.getTimeInMillis()));
            cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 30);
        }
    }

    @OnClick({R.id.appointment_prj_lay, R.id.appointment_time, R.id.appointment_long_lay, R.id.appointment_address_lay, R.id.sure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.appointment_prj_lay:
                showPrjDialog();
                break;
            case R.id.appointment_time:
                showTimeDialog();
                break;
            case R.id.appointment_long_lay:
                showTimeLongDialog();
                break;
            case R.id.appointment_address_lay:
                toShareLocation();
                break;
            case R.id.sure:
                if (!TextUtils.isEmpty(mApplyForUid)) {
                    String prj = mAppointmentPrj.getText().toString();
                    String time = mAppointmentTime.getText().toString();
                    String timeLong = mAppointmentLong.getText().toString();
                    String address = mAppointmentAddress.getText().toString();
                    String remark = mAppointmentRemark.getText().toString();
                    new ApplyForAppointmentTask().request(mApplyForUid, prj, timeLong, time, address, remark);
                }
                break;
        }
    }

    class ApplyForAppointmentTask extends ApplyForAppointmentRequest {
        @Override
        public void onPostExecute(String s) {
            ToastUtil.showMessage(R.string.appointment_apply_for_success);
        }

        @Override
        public void onErrorExecute(String error) {
            ToastUtil.showMessage(R.string.appointment_apply_for_faiure);
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

    /**
     * 显示约会时间
     */
    private void showTimeDialog() {
        initDateTimeView();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tv_appointment_time);
        builder.setView(mDateTimeView);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();

    }

    private void initDateTimeView() {
        mDateTimeView = LayoutInflater.from(this).inflate(R.layout.date_time_picker_layout, null);
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
