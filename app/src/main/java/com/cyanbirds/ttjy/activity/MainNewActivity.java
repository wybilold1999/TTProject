package com.cyanbirds.ttjy.activity;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.base.BaseActivity;
import com.cyanbirds.ttjy.config.AppConstants;
import com.cyanbirds.ttjy.fragment.AboutFragment;
import com.cyanbirds.ttjy.fragment.CardFragment;
import com.cyanbirds.ttjy.fragment.FindLoveFragment;
import com.cyanbirds.ttjy.fragment.FindNewFragment;
import com.cyanbirds.ttjy.fragment.FoundNewFragment;
import com.cyanbirds.ttjy.fragment.SettingFragment;

/**
 * Created by wangyb on 2017/7/3.
 * 描述：
 */

public class MainNewActivity extends BaseActivity {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private Fragment currentFragment;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        initNavigationViewHeader();
        initFragment(savedInstanceState);
    }

    private void initNavigationViewHeader() {
        navigationView = (NavigationView) findViewById(R.id.navigation);
        //设置头像，布局app:headerLayout="@layout/drawer_header"所指定的头布局
        View view = navigationView.inflateHeaderView(R.layout.drawer_header);
        TextView userName = (TextView) view.findViewById(R.id.userName);
        userName.setText(R.string.author);
        //View mNavigationViewHeader = View.inflate(MainActivity.this, R.layout.drawer_header, null);
        //navigationView.addHeaderView(mNavigationViewHeader);//此方法在魅族note 1，头像显示不全
        //菜单点击事件
        navigationView.setNavigationItemSelectedListener(new NavigationItemSelected());
    }

    public void initDrawer(Toolbar toolbar) {
        if (toolbar != null) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                }
            };
            mDrawerToggle.syncState();
            mDrawerLayout.addDrawerListener(mDrawerToggle);
        }
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            currentFragment = new CardFragment();
            switchContent(currentFragment);
        } else {
            //activity销毁后记住销毁前所在页面，用于夜间模式切换
            currentIndex = savedInstanceState.getInt(AppConstants.CURRENT_INDEX);
            switch (this.currentIndex) {
                case 0:
                    currentFragment = new CardFragment();
                    switchContent(currentFragment);
                    break;
                case 1:
                    currentFragment = new FindNewFragment();
                    switchContent(currentFragment);
                    break;
                case 2:
                    currentFragment = new FoundNewFragment();
                    switchContent(currentFragment);
                    break;
                case 3:
                    currentFragment = new FindLoveFragment();
                    switchContent(currentFragment);
                    break;
                case 4:
                    currentFragment = new SettingFragment();
                    switchContent(currentFragment);
                    break;
                case 5:
                    currentFragment = new AboutFragment();
                    switchContent(currentFragment);
                    break;
            }
        }
    }

    class NavigationItemSelected implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            mDrawerLayout.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.navigation_item_1:
                    currentIndex = 0;
                    menuItem.setChecked(true);
                    currentFragment = new CardFragment();
                    switchContent(currentFragment);
                    return true;
                case R.id.navigation_item_2:
                    currentIndex = 1;
                    menuItem.setChecked(true);
                    currentFragment = new FindNewFragment();
                    switchContent(currentFragment);
                    return true;
                case R.id.navigation_item_3:
                    currentIndex = 2;
                    menuItem.setChecked(true);
                    currentFragment = new FoundNewFragment();
                    switchContent(currentFragment);
                    return true;
                case R.id.navigation_item_4:
                    currentIndex = 3;
                    menuItem.setChecked(true);
                    currentFragment = new FindLoveFragment();
                    switchContent(currentFragment);
                    return true;
                case R.id.navigation_item_set:
                    currentIndex = 4;
                    menuItem.setChecked(true);
                    currentFragment = new SettingFragment();
                    switchContent(currentFragment);
                    return true;
                case R.id.navigation_item_about:
                    currentIndex = 5;
                    menuItem.setChecked(true);
                    currentFragment = new AboutFragment();
                    switchContent(currentFragment);
                    return true;
                default:
                    return true;
            }
        }
    }

    public void switchContent(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.contentLayout, fragment).commit();
        invalidateOptionsMenu();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(AppConstants.CURRENT_INDEX, currentIndex);
        super.onSaveInstanceState(outState);
    }
}
