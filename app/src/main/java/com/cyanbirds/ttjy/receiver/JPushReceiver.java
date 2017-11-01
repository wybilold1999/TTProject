package com.cyanbirds.ttjy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.cyanbirds.ttjy.activity.LauncherActivity;
import com.cyanbirds.ttjy.activity.MainActivity;
import com.cyanbirds.ttjy.activity.MainNewActivity;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.manager.NotificationManager;
import com.cyanbirds.ttjy.utils.PushMsgUtil;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class JPushReceiver extends BroadcastReceiver {
	private static final String TAG = "JPush";

	private Handler mHandler = new Handler(Looper.getMainLooper());

	@Override
	public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
			if (AppManager.getClientUser().isShowVip) {
				final String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
				if (!TextUtils.isEmpty(message)) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							PushMsgUtil.getInstance().handlePushMsg(true, message);
						}
					});
				}
			}
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            String msgObject = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (AppManager.isMsgClick) {
				NotificationManager.getInstance().cancelNotification();
			}
			try {
				if (AppManager.getClientUser().isShowVip) {
					if (!TextUtils.isEmpty(msgObject)) {
						JSONObject json = new JSONObject(msgObject);
						final String message = json.getString("message");
						if (!TextUtils.isEmpty(message)) {
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									PushMsgUtil.getInstance().handlePushMsg(false, message);
								}
							});
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
        	//打开自定义的Activity
			String message = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (AppManager.isAppAlive(context, AppManager.pkgName)) {
				if (AppManager.isAppIsInBackground(context)) {
					Intent mainIntent = new Intent();
					if (AppManager.getClientUser().isShowNormal) {
						mainIntent.setClass(context, MainNewActivity.class);
					} else {
						mainIntent.setClass(context, MainActivity.class);
					}
					if (!TextUtils.isEmpty(message)) {
						mainIntent.putExtra(ValueKey.DATA, message);
					}
					mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(mainIntent);
				}
			} else {
				Intent lancherIntent = new Intent();
				if (!TextUtils.isEmpty(message)) {
					lancherIntent.putExtra(ValueKey.DATA, message);
				}
				lancherIntent.setFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				lancherIntent.setClass(context, LauncherActivity.class);
				context.startActivity(intent);
			}
        	
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
        } else {
        }
	}
}
