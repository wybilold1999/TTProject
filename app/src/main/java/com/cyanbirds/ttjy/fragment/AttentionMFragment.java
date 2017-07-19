package com.cyanbirds.ttjy.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.MainNewActivity;
import com.cyanbirds.ttjy.adapter.TabFragmentAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wangyb on 2017/7/4.
 * 描述：
 */

public class AttentionMFragment extends Fragment {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.viewpager)
    ViewPager mViewpager;

    private View rootView;
    private List<String> tabList;
    private List<Fragment> fragmentList;
    private Fragment attentionFrag;//礼物
    private Fragment myAttentionFrag; //喜欢

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_attention, null);
            ButterKnife.bind(this, rootView);
            setupView();
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
        mToolbar.setTitle("关注");
        ((MainNewActivity) getActivity()).initDrawer(mToolbar);
    }

    private void setupView() {
        tabList = new ArrayList<>();
        tabList.add("关注我的");
        tabList.add("我关注的");
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
        mTabLayout.addTab(mTabLayout.newTab().setText(tabList.get(0)));//添加tab选项卡
        mTabLayout.addTab(mTabLayout.newTab().setText(tabList.get(1)));

        fragmentList = new ArrayList<>();

        attentionFrag = new AttentionMeFragment();
        myAttentionFrag = new MyAttentionFragment();
        fragmentList.add(attentionFrag);
        fragmentList.add(myAttentionFrag);

        TabFragmentAdapter fragmentAdapter = new TabFragmentAdapter(
                getChildFragmentManager(), fragmentList, tabList);
        mViewpager.setAdapter(fragmentAdapter);//给ViewPager设置适配器
        mTabLayout.setupWithViewPager(mViewpager);//将TabLayout和ViewPager关联起来。
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }
}
