package com.cyanbirds.ttjy.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.adapter.FoundNewAdapter;
import com.cyanbirds.ttjy.db.ContactSqlManager;
import com.cyanbirds.ttjy.entity.ClientUser;
import com.cyanbirds.ttjy.entity.Contact;
import com.cyanbirds.ttjy.listener.ModifyContactsListener;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.net.request.GetFindLoveRequest;
import com.cyanbirds.ttjy.net.request.GetRealUserRequest;
import com.cyanbirds.ttjy.ui.widget.CircularProgress;
import com.cyanbirds.ttjy.ui.widget.DividerItemDecoration;
import com.cyanbirds.ttjy.ui.widget.WrapperLinearLayoutManager;
import com.cyanbirds.ttjy.utils.DensityUtil;
import com.cyanbirds.ttjy.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wangyb
 * @datetime: 2015-12-20 11:33 GMT+8
 * @email: 395044952@qq.com
 * @description:
 */
public class FoundNewFragment extends Fragment implements ModifyContactsListener.OnDataChangedListener {
    private RecyclerView mRecyclerView;
    private CircularProgress mProgressBar;
    private View rootView;
    private FoundNewAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    private List<ClientUser> mClientUsers;
    private List<Contact> mContacts;
    private int pageIndex = 1;
    private int pageSize = 100;
    private String GENDER = ""; //空表示查询和自己性别相反的用户
    /**
     * 0:同城 1：缘分 2：颜值  -1:就是全国
     */
    private String mUserScopeType = "2";

    private View searchView;
    private RadioButton sex_male;
    private RadioButton sex_female;
    private RadioGroup mSexGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_found, null);
            setupViews();
            setupEvent();
            setupData();
            setHasOptionsMenu(true);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                R.string.tab_found);
        return rootView;
    }

    private void setupViews() {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        mProgressBar = (CircularProgress) rootView.findViewById(R.id.progress_bar);
        layoutManager = new WrapperLinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL, DensityUtil
                .dip2px(getActivity(), 12), DensityUtil.dip2px(
                getActivity(), 12)));
    }

    private void setupEvent(){
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        ModifyContactsListener.getInstance().addOnDataChangedListener(this);
    }

    private void setupData() {
        mClientUsers = new ArrayList<>();
        mAdapter = new FoundNewAdapter(mClientUsers, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mProgressBar.setVisibility(View.VISIBLE);
        if("男".equals(AppManager.getClientUser().sex)){
            GENDER = "FeMale";
        } else {
            GENDER = "Male";
        }
        if("-1".equals(AppManager.getClientUser().userId) ||
                "-2".equals(AppManager.getClientUser().userId) ||
                "-3".equals(AppManager.getClientUser().userId)){ //客服登陆，获取真实用户
            new GetRealLoveUsersTask().request(pageIndex, pageSize, GENDER);
        } else {
            new GetFindLoveTask().request(pageIndex, pageSize, GENDER, mUserScopeType);
        }

        mContacts = ContactSqlManager.getInstance(getActivity()).queryAllContactsByFrom(true);
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        private int lastVisibleItem;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            lastVisibleItem = layoutManager.findLastVisibleItemPosition();
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && lastVisibleItem + 1 == mAdapter.getItemCount()
                    && mAdapter.isShowFooter()) {
                //加载更多
                //请求数据
                if("-1".equals(AppManager.getClientUser().userId) ||
                        "-2".equals(AppManager.getClientUser().userId) ||
                        "-3".equals(AppManager.getClientUser().userId)){ //客服登陆，获取真实用户
                    new GetRealLoveUsersTask().request(++pageIndex, pageSize, GENDER);
                } else {
                    new GetFindLoveTask().request(++pageIndex, pageSize, GENDER, mUserScopeType);
                }
            }
        }
    };

    @Override
    public void onDataChanged(Contact contact) {
        if (contact != null) {
            for (ClientUser clientUser : mClientUsers) {
                if (contact.userId.equals(clientUser.userId)) {
                    mClientUsers.remove(clientUser);
                    break;
                }
            }
            mAdapter.setClientUsers(mClientUsers);
        }
    }

    @Override
    public void onDeleteDataChanged(String userId) {
    }

    @Override
    public void onAddDataChanged(Contact contact) {

    }

    class GetFindLoveTask extends GetFindLoveRequest {
        @Override
        public void onPostExecute(List<ClientUser> userList) {
            if (mContacts != null && mContacts.size() > 0) {
                List<ClientUser> clientUsers = new ArrayList<>();
                clientUsers.addAll(userList);
                for (Contact contact : mContacts) {
                    for (ClientUser clientUser : clientUsers) {
                        if (contact.userId.equals(clientUser.userId)) {
                            userList.remove(clientUser);
                            break;
                        }
                    }
                }
            }

            mProgressBar.setVisibility(View.GONE);
            if (pageIndex == 1) {//进行筛选的时候，滑动到顶部
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
            if(userList == null || userList.size() == 0){
                mAdapter.setIsShowFooter(false);
                mAdapter.notifyDataSetChanged();
                ToastUtil.showMessage(R.string.no_more_data);
            } else {
                mClientUsers.addAll(userList);
                mAdapter.setIsShowFooter(true);
                mAdapter.setClientUsers(mClientUsers);
            }
        }

        @Override
        public void onErrorExecute(String error) {
            ToastUtil.showMessage(error);
            mProgressBar.setVisibility(View.GONE);
            mAdapter.setIsShowFooter(false);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 获取真实的用户
     */
    class GetRealLoveUsersTask extends GetRealUserRequest {
        @Override
        public void onPostExecute(List<ClientUser> clientUsers) {
            mProgressBar.setVisibility(View.GONE);
            if(clientUsers == null || clientUsers.size() == 0){
                mAdapter.setIsShowFooter(false);
                mAdapter.notifyDataSetChanged();
//                ToastUtil.showMessage(R.string.no_more_data);
            } else {
                mClientUsers.addAll(clientUsers);
                mAdapter.setIsShowFooter(true);
                mAdapter.setClientUsers(mClientUsers);
            }
        }

        @Override
        public void onErrorExecute(String error) {
            mProgressBar.setVisibility(View.GONE);
            mAdapter.setIsShowFooter(false);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showSearchDialog();
        return super.onOptionsItemSelected(item);
    }

    /**
     * 筛选dialog
     */
    private void showSearchDialog(){
        initSearchDialogView();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.search_option));
        builder.setView(searchView);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        pageIndex = 1;
                        mClientUsers.clear();
                        if("-1".equals(AppManager.getClientUser().userId) ||
                                "-2".equals(AppManager.getClientUser().userId) ||
                                "-3".equals(AppManager.getClientUser().userId)){ //客服登陆，获取真实用户
                            new GetRealLoveUsersTask().request(pageIndex, pageSize, GENDER);
                        } else {
                            new GetFindLoveTask().request(pageIndex, pageSize, GENDER, mUserScopeType);
                        }
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    /**
     * 初始化筛选对话框
     */
    private void initSearchDialogView(){
        searchView = LayoutInflater.from(getActivity()).inflate(R.layout.item_search, null);
        mSexGroup = (RadioGroup) searchView.findViewById(R.id.rg_sex);
        sex_male = (RadioButton) searchView.findViewById(R.id.sex_male);
        sex_female = (RadioButton) searchView.findViewById(R.id.sex_female);
        if("Male".equals(GENDER)){
            sex_male.setChecked(true);
        } else if("FeMale".equals(GENDER)){
            sex_female.setChecked(true);
        }
        mSexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == sex_male.getId()){
                    GENDER = "Male";
                } else if(checkedId == sex_female.getId()){
                    GENDER = "FeMale";
                }
            }
        });
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
    public void onDestroy() {
        super.onDestroy();
        ModifyContactsListener.getInstance().removeOnDataChangedListener(this);
    }
}
