package com.cyanbirds.ttjy.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.FindPwdActivity;
import com.cyanbirds.ttjy.activity.MainNewActivity;
import com.cyanbirds.ttjy.activity.ModifyPwdActivity;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.db.ConversationSqlManager;
import com.cyanbirds.ttjy.db.IMessageDaoManager;
import com.cyanbirds.ttjy.db.MyGoldDaoManager;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.manager.NotificationManager;
import com.cyanbirds.ttjy.net.request.LogoutRequest;
import com.cyanbirds.ttjy.utils.PreferencesUtils;
import com.cyanbirds.ttjy.utils.ProgressDialogUtils;
import com.cyanbirds.ttjy.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cyanbirds.ttjy.activity.base.BaseActivity.exitApp;
import static com.cyanbirds.ttjy.activity.base.BaseActivity.finishAll;

/**
 * Created by wangyb on 2017/6/29.
 * 描述：
 */

public class SettingFragment extends Fragment {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.switch_msg)
    SwitchCompat mSwitchMsg;
    @BindView(R.id.switch_msg_content)
    SwitchCompat mSwitchMsgContent;
    @BindView(R.id.switch_voice)
    SwitchCompat mSwitchVoice;
    @BindView(R.id.switch_vibrate)
    SwitchCompat mSwitchVibrate;
    @BindView(R.id.is_bangding_phone)
    TextView mIsBangdingPhone;
    @BindView(R.id.banding_phone_lay)
    RelativeLayout mBandingPhoneLay;
    @BindView(R.id.modify_pwd_lay)
    RelativeLayout mModifyPwdLay;
    @BindView(R.id.quit)
    RelativeLayout mQuit;

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_setting, null);
            ButterKnife.bind(this, rootView);
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
        mToolbar.setTitle("设置");
        ((MainNewActivity) getActivity()).initDrawer(mToolbar);
    }

    private void setupData() {
        if (AppManager.getClientUser().isCheckPhone) {
            mIsBangdingPhone.setText(R.string.already_bangding);
        } else {
            mIsBangdingPhone.setText(R.string.un_bangding);
        }
        if (PreferencesUtils.getNewMessageNotice(getActivity())) {
            mSwitchMsg.setChecked(true);
        } else {
            mSwitchMsg.setChecked(false);
        }
        if (PreferencesUtils.getShowMessageInfo(getActivity())) {
            mSwitchMsgContent.setChecked(true);
        } else {
            mSwitchMsgContent.setChecked(false);
        }
        if (PreferencesUtils.getNoticeVoice(getActivity())) {
            mSwitchVoice.setChecked(true);
        } else {
            mSwitchVoice.setChecked(false);
        }
        if (PreferencesUtils.getNoticeShock(getActivity())) {
            mSwitchVibrate.setChecked(true);
        } else {
            mSwitchVibrate.setChecked(false);
        }
    }


    @OnClick({R.id.switch_msg, R.id.switch_msg_content, R.id.switch_voice, R.id.switch_vibrate, R.id.banding_phone_lay, R.id.modify_pwd_lay, R.id.quit})
    public void onViewClicked(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.switch_msg:
                if (PreferencesUtils.getNewMessageNotice(getActivity())) {
                    mSwitchMsg.setChecked(false);
                    PreferencesUtils.setNewMessageNotice(getActivity(), false);
                } else {
                    mSwitchMsg.setChecked(true);
                    PreferencesUtils.setNewMessageNotice(getActivity(), true);
                }
                break;
            case R.id.switch_msg_content:
                if (PreferencesUtils.getShowMessageInfo(getActivity())) {
                    mSwitchMsgContent.setChecked(false);
                    PreferencesUtils.setShowMessageInfo(getActivity(), false);
                } else {
                    mSwitchMsgContent.setChecked(true);
                    PreferencesUtils.setShowMessageInfo(getActivity(), true);
                }
                break;
            case R.id.switch_voice:
                if (PreferencesUtils.getNoticeVoice(getActivity())) {
                    mSwitchVoice.setChecked(false);
                    PreferencesUtils.setNoticeVoice(getActivity(), false);
                } else {
                    mSwitchVoice.setChecked(true);
                    PreferencesUtils.setNoticeVoice(getActivity(), true);
                }
                break;
            case R.id.switch_vibrate:
                if (PreferencesUtils.getNoticeShock(getActivity())) {
                    mSwitchVibrate.setChecked(false);
                    PreferencesUtils.setNoticeShock(getActivity(), false);
                } else {
                    mSwitchVibrate.setChecked(true);
                    PreferencesUtils.setNoticeShock(getActivity(), true);
                }
                break;
            case R.id.banding_phone_lay:
                //0=注册1=找回密码2=验证绑定手机
                intent.setClass(getActivity(), FindPwdActivity.class);
                intent.putExtra(ValueKey.INPUT_PHONE_TYPE, 2);
                startActivity(intent);
                break;
            case R.id.modify_pwd_lay:
                intent.setClass(getActivity(), ModifyPwdActivity.class);
                startActivity(intent);
                break;
            case R.id.quit:
                showQuitDialog();
                break;
        }
    }

    class LogoutTask extends LogoutRequest {
        @Override
        public void onPostExecute(String s) {
            ProgressDialogUtils.getInstance(getActivity()).dismiss();
            MobclickAgent.onProfileSignOff();
            release();
            NotificationManager.getInstance().cancelNotification();
            finishAll();
            PreferencesUtils.setIsLogin(getActivity(), false);
            Intent intent = getActivity().getPackageManager()
                    .getLaunchIntentForPackage(
                            getActivity().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        @Override
        public void onErrorExecute(String error) {
            ProgressDialogUtils.getInstance(getActivity()).dismiss();
            ToastUtil.showMessage(error);
        }
    }

    /**
     * 显示退出dialog
     */
    private void showQuitDialog() {
        new AlertDialog.Builder(getActivity())
                .setItems(R.array.quit_items,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                switch (which) {
                                    case 0:
                                        ProgressDialogUtils.getInstance(getActivity()).show(R.string.dialog_logout_tips);
                                        new LogoutTask().request();
                                        break;
                                    case 1:
                                        exitApp();
                                        break;
                                }
                            }
                        }).setTitle(R.string.quit).show();
    }


    /**
     * 释放数据库
     */
    private static void release() {
        IMessageDaoManager.reset();
        ConversationSqlManager.reset();
        MyGoldDaoManager.reset();
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
    public void onDestroyView() {
        super.onDestroyView();
    }
}
