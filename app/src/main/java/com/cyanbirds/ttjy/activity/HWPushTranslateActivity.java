package com.cyanbirds.ttjy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.cyanbirds.ttjy.CSApplication;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.db.ConversationSqlManager;
import com.cyanbirds.ttjy.entity.ClientUser;
import com.cyanbirds.ttjy.entity.Conversation;
import com.cyanbirds.ttjy.entity.PushMsgModel;
import com.cyanbirds.ttjy.helper.AppActivityLifecycleCallbacks;
import com.cyanbirds.ttjy.listener.MessageChangedListener;
import com.cyanbirds.ttjy.listener.MessageUnReadListener;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.service.ConnectServerService;
import com.cyanbirds.ttjy.utils.PreferencesUtils;
import com.cyanbirds.ttjy.utils.PushMsgUtil;
import com.google.gson.Gson;

/**
 * Created by wangyb on 2018/10/31
 */
public class HWPushTranslateActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String content = getIntent().getData().getQueryParameter("action");

        if (AppManager.isServiceRunning(this, ConnectServerService.class.getName())) {
//            Log.e("hw_test", "----------------------1------------------");
            if (!AppActivityLifecycleCallbacks.getInstance().getIsForeground()) {
//                Log.e("hw_test", "----------------------2------------------");
                if (PreferencesUtils.getIsLogin(this)) {
                    Gson gson = new Gson();
                    PushMsgModel pushMsgModel = gson.fromJson(content, PushMsgModel.class);

                    PushMsgUtil.getInstance().handlePushMsg(false, content);
                    Conversation conversation = ConversationSqlManager.getInstance(CSApplication.getInstance())
                            .queryConversationForByTalkerId(pushMsgModel.sender);
                    if (conversation != null) {
                        conversation.unreadCount = 0;
                        ConversationSqlManager.getInstance(this).updateConversation(conversation);
                        MessageUnReadListener.getInstance().notifyDataSetChanged(0);
                        MessageChangedListener.getInstance().notifyMessageChanged("");
                    }
                    Intent chatIntent = new Intent(this, ChatActivity.class);
                    ClientUser clientUser = new ClientUser();
                    clientUser.userId = pushMsgModel.sender;
                    clientUser.user_name = pushMsgModel.senderName;
                    chatIntent.putExtra(ValueKey.USER, clientUser);
                    chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(chatIntent);
//                    Log.e("hw_test", "----------------------3------------------");
                } else {
//                    Log.e("hw_test", "----------------------4------------------");
                    toLaunch(content);
                }
            } else {
//                Log.e("hw_test", "----------------------5------------------");
                PushMsgUtil.getInstance().handlePushMsg(false, content);
            }
        } else {
//            Log.e("hw_test", "----------------------6------------------");
            toLaunch(content);
        }
        finish();
    }

    private void toLaunch(String content) {
        Intent intent = new Intent();
        if (!TextUtils.isEmpty(content)) {
            intent.putExtra(ValueKey.DATA, content);
        }
        intent.setClass(this, LauncherActivity.class);
        startActivity(intent);
    }
}
