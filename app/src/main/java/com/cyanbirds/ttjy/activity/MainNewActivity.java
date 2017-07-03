package com.cyanbirds.ttjy.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
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
import com.cyanbirds.ttjy.entity.FederationToken;
import com.cyanbirds.ttjy.fragment.AboutFragment;
import com.cyanbirds.ttjy.fragment.AttentionMeFragment;
import com.cyanbirds.ttjy.fragment.CardFragment;
import com.cyanbirds.ttjy.fragment.FindLoveFragment;
import com.cyanbirds.ttjy.fragment.FindNewFragment;
import com.cyanbirds.ttjy.fragment.FoundNewFragment;
import com.cyanbirds.ttjy.fragment.GiftsFragment;
import com.cyanbirds.ttjy.fragment.LoveMeFragment;
import com.cyanbirds.ttjy.fragment.MessageFragment;
import com.cyanbirds.ttjy.fragment.SettingFragment;
import com.cyanbirds.ttjy.helper.SDKCoreHelper;
import com.cyanbirds.ttjy.listener.MessageUnReadListener;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.manager.NotificationManager;
import com.cyanbirds.ttjy.net.request.GetCityInfoRequest;
import com.cyanbirds.ttjy.net.request.GetOSSTokenRequest;
import com.cyanbirds.ttjy.net.request.UploadCityInfoRequest;
import com.cyanbirds.ttjy.service.MyIntentService;
import com.cyanbirds.ttjy.service.MyPushService;
import com.cyanbirds.ttjy.utils.PreferencesUtils;
import com.cyanbirds.ttjy.utils.PushMsgUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.igexin.sdk.PushManager;
import com.umeng.analytics.MobclickAgent;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.yuntongxun.ecsdk.ECInitParams;

import java.util.LinkedHashSet;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

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

    private ClientConfiguration mOSSConf;

    private static final int REQUEST_PERMISSION = 0;
    private final int REQUEST_LOCATION_PERMISSION = 1000;
    private final int REQUEST_PERMISSION_SETTING = 10001;

    private static final int MSG_SET_ALIAS = 1001;//极光推送设置别名
    private static final int MSG_SET_TAGS = 1002;//极光推送设置tag

    private AMapLocationClientOption mLocationOption;
    private AMapLocationClient mlocationClient;
    private boolean isSecondAccess = false;

    private String curLat;
    private String curLon;
    private String currentCity;

    /**
     * oss鉴权获取失败重试次数
     */
    public int mOSSTokenRetryCount = 0;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    JPushInterface.setAliasAndTags(getApplicationContext(), null, null, mAliasCallback);
                    JPushInterface.setAliasAndTags(getApplicationContext(), (String) msg.obj, null, mAliasCallback);
                    break;
                case MSG_SET_TAGS:
                    JPushInterface.setAliasAndTags(getApplicationContext(), null, null, mAliasCallback);
                    JPushInterface.setAliasAndTags(getApplicationContext(), null, (Set<String>) msg.obj, mAliasCallback);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        new GetCityInfoTask().request();
        initNavigationViewHeader();
        initFragment(savedInstanceState);
        initOSS();
        SDKCoreHelper.init(this, ECInitParams.LoginMode.FORCE_LOGIN);
        updateConversationUnRead();

        AppManager.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                /**
                 * 注册小米推送
                 */
                MiPushClient.registerPush(MainNewActivity.this, AppConstants.MI_PUSH_APP_ID, AppConstants.MI_PUSH_APP_KEY);
                //个推
                initGeTuiPush();

                initJPush();


                loadData();

                initLocationClient();
            }
        });

        AppManager.requestLocationPermission(this);
        requestPermission();
    }

    private void initNavigationViewHeader() {
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
        LinearLayout redPoint = (LinearLayout) navigationView.getMenu().findItem(R.id.navigation_item_5).getActionView();
        msgUnreadCount = (TextView) redPoint.findViewById(R.id.msg_unread_count);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, PersonalInfoActivity.class);
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
                    currentFragment = new MessageFragment();
                    switchContent(currentFragment);
                    break;
                case 5:
                    currentFragment = new GiftsFragment();
                    switchContent(currentFragment);
                    break;
                case 6:
                    currentFragment = new AttentionMeFragment();
                    switchContent(currentFragment);
                    break;
                case 7:
                    currentFragment = new LoveMeFragment();
                    switchContent(currentFragment);
                    break;
                case 8:
                    currentFragment = new SettingFragment();
                    switchContent(currentFragment);
                    break;
                case 9:
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
                case R.id.navigation_item_5:
                    currentIndex = 4;
                    menuItem.setChecked(true);
                    currentFragment = new MessageFragment();
                    switchContent(currentFragment);
                    return true;
                case R.id.navigation_item_6:
                    currentIndex = 5;
                    menuItem.setChecked(true);
                    currentFragment = new GiftsFragment();
                    switchContent(currentFragment);
                    return true;
                case R.id.navigation_item_7:
                    currentIndex = 6;
                    menuItem.setChecked(true);
                    currentFragment = new AttentionMeFragment();
                    switchContent(currentFragment);
                    return true;
                case R.id.navigation_item_8:
                    currentIndex = 7;
                    menuItem.setChecked(true);
                    currentFragment = new LoveMeFragment();
                    switchContent(currentFragment);
                    return true;
                case R.id.navigation_item_set:
                    currentIndex = 8;
                    menuItem.setChecked(true);
                    currentFragment = new SettingFragment();
                    switchContent(currentFragment);
                    return true;
                case R.id.navigation_item_about:
                    currentIndex = 9;
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

    /**
     * 初始化oss
     */
    private void initOSS() {
        mOSSConf = new ClientConfiguration();
        mOSSConf.setConnectionTimeout(30 * 1000); // 连接超时，默认15秒
        mOSSConf.setSocketTimeout(30 * 1000); // socket超时，默认15秒
        mOSSConf.setMaxConcurrentRequest(50); // 最大并发请求书，默认5个
        mOSSConf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSSLog.enableLog();

        final Handler handler = new Handler();
        // 每30分钟请求一次鉴权
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new GetFederationTokenTask().request();
                handler.postDelayed(this, 60 * 30 * 1000);
            }
        };

        handler.postDelayed(runnable, 0);
    }

    class GetFederationTokenTask extends GetOSSTokenRequest {

        @Override
        public void onPostExecute(FederationToken result) {
            try {
                if (result != null) {
                    AppManager.setFederationToken(result);
                    OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(result.accessKeyId, result.accessKeySecret, result.securityToken);
                    OSS oss = new OSSClient(getApplicationContext(), result.endpoint, credentialProvider, mOSSConf);
                    AppManager.setOSS(oss);
                    mOSSTokenRetryCount = 0;
                } else {
                    if (mOSSTokenRetryCount < 5) {
                        new GetFederationTokenTask().request();
                        mOSSTokenRetryCount++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorExecute(String error) {
            if (mOSSTokenRetryCount < 5) {
                new GetFederationTokenTask().request();
                mOSSTokenRetryCount++;
            }
        }
    }

    /**
     * 个推注册
     */
    private void initGeTuiPush() {
        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作
        PushManager.getInstance().initialize(this.getApplicationContext(), MyPushService.class);
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), MyIntentService.class);
    }

    private void initJPush() {
        // 初始化 JPush
        JPushInterface.init(this);
//		JPushInterface.setDebugMode(true);

        if (!PreferencesUtils.getJpushSetAliasState(this)) {
            //调用JPush API设置Alias
            mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, AppManager.getClientUser().userId));
            //调用JPush API设置Tag
            Set<String> tag = new LinkedHashSet<>(1);
            if ("男".equals(AppManager.getClientUser().sex)) {
                tag.add("female");
            } else {
                tag.add("male");
            }
            mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_TAGS, tag));
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null && !TextUtils.isEmpty(aMapLocation.getCity())) {
            AppManager.getClientUser().latitude = String.valueOf(aMapLocation.getLatitude());
            AppManager.getClientUser().longitude = String.valueOf(aMapLocation.getLongitude());
            new UploadCityInfoTask().request(aMapLocation.getCity(),
                    AppManager.getClientUser().latitude, AppManager.getClientUser().longitude);
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

    /**
     * 极光推送设置别名后的回调
     */
    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            switch (code) {
                case 0:
                    //Set tag and alias success
                    PreferencesUtils.setJpushSetAliasState(MainNewActivity.this, true);
                    break;

                case 6002:
                    //"Failed to set alias and tags due to timeout. Try again after 60s.";
                    ConnectivityManager conn = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = conn.getActiveNetworkInfo();
                    if (info != null && info.isConnected()) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_TAGS, tags), 1000 * 60);
                    }
                    break;
            }
        }
    };

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
