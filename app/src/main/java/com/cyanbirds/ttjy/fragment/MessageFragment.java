package com.cyanbirds.ttjy.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.MainNewActivity;
import com.cyanbirds.ttjy.adapter.MessageAdapter;
import com.cyanbirds.ttjy.db.ConversationSqlManager;
import com.cyanbirds.ttjy.entity.Conversation;
import com.cyanbirds.ttjy.listener.MessageChangedListener;
import com.cyanbirds.ttjy.ui.widget.DividerItemDecoration;
import com.cyanbirds.ttjy.ui.widget.WrapperLinearLayoutManager;
import com.cyanbirds.ttjy.utils.DensityUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

/**
 * @author: wangyb
 * @datetime: 2015-12-20 11:34 GMT+8
 * @email: 395044952@qq.com
 * @description:
 */
public class MessageFragment extends Fragment implements MessageChangedListener.OnMessageChangedListener {
    private View rootView;
    private Toolbar mToolbar;
    private RecyclerView mMessageRecyclerView;
    private MessageAdapter mAdapter;
    private List<Conversation> mConversations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_message, null);
            setupViews();
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

    private void setupViews(){
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar_actionbar);
        mMessageRecyclerView = (RecyclerView) rootView.findViewById(R.id.message_recycler_view);
        LinearLayoutManager layoutManager = new WrapperLinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMessageRecyclerView.setLayoutManager(layoutManager);
        mMessageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mMessageRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL, DensityUtil
                .dip2px(getActivity(), 12), DensityUtil.dip2px(
                getActivity(), 12)));
    }

    private void setupEvent(){
        MessageChangedListener.getInstance().setMessageChangedListener(this);
    }

    private void setupData(){
        mAdapter = new MessageAdapter(getActivity(), mConversations);
        mMessageRecyclerView.setAdapter(mAdapter);
        getData();
    }

    private void getData(){
        mConversations = ConversationSqlManager.getInstance(getActivity()).queryConversations();
        if(mConversations != null && !mConversations.isEmpty()){
            mAdapter.setConversations(mConversations);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onMessageChanged(String conversationId) {
        getData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageChangedListener.getInstance().setMessageChangedListener(null);
        MessageChangedListener.getInstance().clearAllMessageChangedListener();
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
}
