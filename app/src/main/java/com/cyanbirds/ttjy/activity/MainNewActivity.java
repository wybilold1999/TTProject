package com.cyanbirds.ttjy.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.base.BaseActivity;
import com.cyanbirds.ttjy.config.AppConstants;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.db.ConversationSqlManager;
import com.cyanbirds.ttjy.entity.CityInfo;
import com.cyanbirds.ttjy.entity.ClientUser;
import com.cyanbirds.ttjy.fragment.AboutFragment;
import com.cyanbirds.ttjy.fragment.AttentionMFragment;
import com.cyanbirds.ttjy.fragment.CardFragment;
import com.cyanbirds.ttjy.fragment.FoundGridFragment;
import com.cyanbirds.ttjy.fragment.GiftLoveFragment;
import com.cyanbirds.ttjy.fragment.MessageNewFragment;
import com.cyanbirds.ttjy.fragment.SettingFragment;
import com.cyanbirds.ttjy.helper.SDKCoreHelper;
import com.cyanbirds.ttjy.listener.MessageUnReadListener;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.manager.NotificationManager;
import com.cyanbirds.ttjy.net.request.GetCityInfoRequest;
import com.cyanbirds.ttjy.net.request.UploadCityInfoRequest;
import com.cyanbirds.ttjy.utils.PreferencesUtils;
import com.cyanbirds.ttjy.utils.PushMsgUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.umeng.analytics.MobclickAgent;
import com.yuntongxun.ecsdk.ECInitParams;

/**
 * Created by wangyb on 2017/7/3.
 * 描述：
 */

public class MainNewActivity extends BaseActivity implements View.OnClickListener, MessageUnReadListener.OnMessageUnReadListener, AMapLocationListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private Fragment currentFragment;
    private int currentIndex;

    private TextView msgUnreadCount;
    private ImageView mGiftUnread;
    private ImageView mAttentionUnread;

    private static final int REQUEST_PERMISSION = 0;
    private final int REQUEST_LOCATION_PERMISSION = 1000;
    private final int REQUEST_PERMISSION_SETTING = 10001;

    private AMapLocationClientOption mLocationOption;
    private AMapLocationClient mlocationClient;
    private boolean isSecondAccess = false;

    private String curLat;
    private String curLon;
    private String currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        new GetCityInfoTask().request();
        initNavigationViewHeader();
        initFragment(savedInstanceState);
        setupEvent();
        SDKCoreHelper.init(this, ECInitParams.LoginMode.FORCE_LOGIN);
        updateConversationUnRead();

        loadData();
        initLocationClient();

        AppManager.requestLocationPermission(this);
        requestPermission();
    }

    private void initNavigationViewHeader() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation);
        //设置头像，布局app:headerLayout="@layout/drawer_header"所指定的头布局
        View view = navigationView.inflateHeaderView(R.layout.drawer_header);
        TextView userName = (TextView) view.findViewById(R.id.user_name);
        SimpleDraweeView portrait = (SimpleDraweeView) view.findViewById(R.id.portrait);
        LinearLayout infoLay = (LinearLayout) view.findViewById(R.id.info_lay);

        infoLay.setOnClickListener(this);

        if (!TextUtils.isEmpty(AppManager.getClientUser().user_name)) {
            userName.setText(AppManager.getClientUser().user_name);
        }
        if (!TextUtils.isEmpty(AppManager.getClientUser().face_url)) {
            portrait.setImageURI(Uri.parse(AppManager.getClientUser().face_url));
        }

        //菜单点击事件
        navigationView.setNavigationItemSelectedListener(new NavigationItemSelected());

        initMsgUnreadView();
    }

    /**
     * 初始化未读消息组件
     */
    private void initMsgUnreadView() {
        LinearLayout msgredPoint = (LinearLayout) navigationView.getMenu().findItem(R.id.navigation_item_4).getActionView();
        msgUnreadCount = (TextView) msgredPoint.findViewById(R.id.msg_unread_count);

        LinearLayout giftredPoint = (LinearLayout) navigationView.getMenu().findItem(R.id.navigation_item_5).getActionView();
        mGiftUnread = (ImageView) giftredPoint.findViewById(R.id.gift_unread);

        LinearLayout attentionredPoint = (LinearLayout) navigationView.getMenu().findItem(R.id.navigation_item_6).getActionView();
        mAttentionUnread = (ImageView) attentionredPoint.findViewById(R.id.attention_unread);

    }

    private void setupEvent() {
        MessageUnReadListener.getInstance().setMessageUnReadListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, PersonalInfoNewActivity.class);
        intent.putExtra(ValueKey.USER_ID, AppManager.getClientUser().userId);
        startActivity(intent);
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
                    currentFragment = new FoundGridFragment();
                    switchContent(currentFragment);
                    break;
                /*case 2:
                    currentFragment = new FindNewFragment();
                    switchContent(currentFragment);
                    break;*/
                case 3:
                    currentFragment = new MessageNewFragment();
                    switchContent(currentFragment);
                    break;
                case 4:
                    currentFragment = new GiftLoveFragment();
                    switchContent(currentFragment);
                    break;
                case 5:
                    currentFragment = new AttentionMFragment();
                    switchContent(currentFragment);
                    break;
                case 6:
                    currentFragment = new SettingFragment();
                    switchContent(currentFragment);
                    break;
                case 7:
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
                    currentFragment = new FoundGridFragment();
                    switchContent(currentFragment);
                    return true;
                /*case R.id.navigation_item_3:
                    currentIndex = 2;
                    menuItem.setChecked(true);
                    currentFragment = new FindNewFragment();
                    switchContent(currentFragment);
                    return true;*/
                case R.id.navigation_item_4:
                    currentIndex = 3;
                    menuItem.setChecked(true);
                    currentFragment = new MessageNewFragment();
                    switchContent(currentFragment);
                    return true;
                case R.id.navigation_item_5:
                    currentIndex = 4;
                    menuItem.setChecked(true);
                    currentFragment = new GiftLoveFragment();
                    switchContent(currentFragment);
                    mGiftUnread.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_item_6:
                    currentIndex = 5;
                    menuItem.setChecked(true);
                    currentFragment = new AttentionMFragment();
                    switchContent(currentFragment);
                    mAttentionUnread.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_item_set:
                    currentIndex = 6;
                    menuItem.setChecked(true);
                    currentFragment = new SettingFragment();
                    switchContent(currentFragment);
                    return true;
                case R.id.navigation_item_about:
                    currentIndex = 7;
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

    /**
     * 初始化定位
     */
    private void initLocationClient() {
        mlocationClient = new AMapLocationClient(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(this);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取最近3s内精度最高的一次定位结果：
        mLocationOption.setOnceLocationLatest(true);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        //启动定位
        mlocationClient.startLocation();
    }

    /**
     * 请求读写文件夹的权限
     */
    private void requestPermission() {
        PackageManager pkgManager = getPackageManager();
        // 读写 sd card 权限非常重要, android6.0默认禁止的, 建议初始化之前就弹窗让用户赋予该权限
        boolean sdCardWritePermission =
                pkgManager.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, getPackageName()) == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= 23 && !sdCardWritePermission) {
            //请求权限
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }
    }

    /**
     * 点击通知栏的消息，将消息入库
     */
    private void loadData() {
        String msg = getIntent().getStringExtra(ValueKey.DATA);
        if (!TextUtils.isEmpty(msg)) {
            PushMsgUtil.getInstance().handlePushMsg(false, msg);
            NotificationManager.getInstance().cancelNotification();
            AppManager.isMsgClick = true;
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null && !TextUtils.isEmpty(aMapLocation.getCity())) {
            new UploadCityInfoTask().request(aMapLocation.getCity(),
                    String.valueOf(aMapLocation.getLatitude()), String.valueOf(aMapLocation.getLongitude()));
            PreferencesUtils.setCurrentCity(this, aMapLocation.getCity());
            ClientUser clientUser = AppManager.getClientUser();
            clientUser.latitude = String.valueOf(aMapLocation.getLatitude());
            clientUser.longitude = String.valueOf(aMapLocation.getLongitude());
            AppManager.setClientUser(clientUser);
        } else {
            new UploadCityInfoTask().request(currentCity, curLat, curLon);
        }
    }

    /**
     * 获取用户所在城市
     */
    class GetCityInfoTask extends GetCityInfoRequest {

        @Override
        public void onPostExecute(CityInfo cityInfo) {
            if (cityInfo != null) {
                try {
                    currentCity = cityInfo.city;
                    String[] rectangle = cityInfo.rectangle.split(";");
                    String[] leftBottom = rectangle[0].split(",");
                    String[] rightTop = rectangle[1].split(",");

                    double lat = Double.parseDouble(leftBottom[1]) + (Double.parseDouble(rightTop[1]) - Double.parseDouble(leftBottom[1])) / 5;
                    curLat = String.valueOf(lat);

                    double lon = Double.parseDouble(leftBottom[0]) + (Double.parseDouble(rightTop[0]) - Double.parseDouble(leftBottom[0])) / 5;
                    curLon = String.valueOf(lon);
                    AppManager.getClientUser().latitude = curLat;
                    AppManager.getClientUser().longitude = curLon;
                } catch (Exception e) {

                }
            }
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }

    class UploadCityInfoTask extends UploadCityInfoRequest {

        @Override
        public void onPostExecute(String isShow) {
            if ("0".equals(isShow)) {
                AppManager.getClientUser().isShowDownloadVip = false;
                AppManager.getClientUser().isShowGold = false;
                AppManager.getClientUser().isShowLovers = false;
                AppManager.getClientUser().isShowMap = false;
                AppManager.getClientUser().isShowVideo = false;
                AppManager.getClientUser().isShowVip = false;
                AppManager.getClientUser().isShowRpt = false;
            }
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }

    @Override
    public void notifyUnReadChanged(int type) {
        updateConversationUnRead();
    }

    /**
     * 更新会话未读消息总数
     */
    private void updateConversationUnRead() {
        int total = ConversationSqlManager.getInstance(this)
                .getAnalyticsUnReadConversation();
        msgUnreadCount.setVisibility(View.GONE);
        if (total > 0) {
            if (total >= 100) {
                msgUnreadCount.setText(String.valueOf("99+"));
            } else {
                msgUnreadCount.setText(String.valueOf(total));
            }
            msgUnreadCount.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {

        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // 拒绝授权
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // 勾选了不再提示
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showOpenLocationDialog();
                } else {
                    if (!isSecondAccess) {
                        showAccessLocationDialog();
                    }
                }
            } else {
                initLocationClient();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showOpenLocationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.open_location);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);

            }
        });
        builder.show();
    }


    private void showAccessLocationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.access_location);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isSecondAccess = true;
                if (Build.VERSION.SDK_INT >= 23) {
                    ActivityCompat.requestPermissions(MainNewActivity.this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
                }

            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getName());
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            moveTaskToBack(false);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            initLocationClient();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(AppConstants.CURRENT_INDEX, currentIndex);
        super.onSaveInstanceState(outState);
    }
}
