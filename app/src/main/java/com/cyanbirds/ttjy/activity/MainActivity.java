package com.cyanbirds.ttjy.activity;

import android.Manifest;
import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.cyanbirds.ttjy.CSApplication;
import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.base.BaseActivity;
import com.cyanbirds.ttjy.adapter.ViewPagerAdapter;
import com.cyanbirds.ttjy.db.ConversationSqlManager;
import com.cyanbirds.ttjy.entity.ClientUser;
import com.cyanbirds.ttjy.fragment.FoundFragment;
import com.cyanbirds.ttjy.fragment.HomeLoveFragment;
import com.cyanbirds.ttjy.fragment.MessageFragment;
import com.cyanbirds.ttjy.fragment.PersonalFragment;
import com.cyanbirds.ttjy.helper.BottomNavigationViewHelper;
import com.cyanbirds.ttjy.helper.SDKCoreHelper;
import com.cyanbirds.ttjy.listener.MessageUnReadListener;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.net.IUserApi;
import com.cyanbirds.ttjy.net.base.RetrofitFactory;
import com.cyanbirds.ttjy.ui.widget.CustomViewPager;
import com.cyanbirds.ttjy.utils.CheckUtil;
import com.cyanbirds.ttjy.utils.DensityUtil;
import com.cyanbirds.ttjy.utils.PreferencesUtils;
import com.cyanbirds.ttjy.utils.Utils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import com.umeng.analytics.MobclickAgent;
import com.yuntongxun.ecsdk.ECInitParams;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class MainActivity extends BaseActivity implements MessageUnReadListener.OnMessageUnReadListener, AMapLocationListener {

	private CustomViewPager viewPager;
	private BottomNavigationView bottomNavigationView;

	private final int REQUEST_LOCATION_PERMISSION = 1000;

	private AMapLocationClientOption mLocationOption;
	private AMapLocationClient mlocationClient;
	private boolean isSecondAccess = false;

	private String curLat;
	private String curLon;

	private Badge mBadgeView;
	private QBadgeView mQBadgeView;
	private RxPermissions rxPermissions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		setupViews();
		setupEvent();
		if (AppManager.getClientUser().is_vip) {
			SDKCoreHelper.init(CSApplication.getInstance(), ECInitParams.LoginMode.FORCE_LOGIN);
		}
		updateConversationUnRead();

		locationSuccess();
	}

	/**
	 * 判断是否定位成功。成功就不定位了，直接上传城市
	 */
	private void locationSuccess() {
		String currentCity = AppManager.getClientUser().currentCity;
		curLat = AppManager.getClientUser().latitude;
		curLon = AppManager.getClientUser().longitude;
		if (!TextUtils.isEmpty(currentCity) && !TextUtils.isEmpty(curLat) && !TextUtils.isEmpty(curLon)) {
			uploadCityInfoRequest(currentCity, curLat, curLon);
		} else {
			initLocationClient();
			requestLocationPermission();
		}
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
	}

	/**
	 * 开始定位
	 */
	private void startLocation() {
		//设置定位参数
		mlocationClient.setLocationOption(mLocationOption);
		//启动定位
		mlocationClient.startLocation();
	}

	/**
	 * 停止定位
	 */
	private void stopLocation(){
		// 停止定位
		mlocationClient.stopLocation();
	}

	/**
	 * 销毁定位
	 */
	private void destroyLocation(){
		if (null != mlocationClient) {
			/**
			 * 如果AMapLocationClient是在当前Activity实例化的，
			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
			 */
			mlocationClient.onDestroy();
			mlocationClient = null;
			mLocationOption = null;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);// 必须要调用这句(信鸽推送)
	}

	@Override
	public void onLocationChanged(AMapLocation aMapLocation) {
		if (aMapLocation != null && !TextUtils.isEmpty(aMapLocation.getCity())) {
			PreferencesUtils.setCurrentCity(this, aMapLocation.getCity());
			ClientUser clientUser = AppManager.getClientUser();
			clientUser.latitude = String.valueOf(aMapLocation.getLatitude());
			clientUser.longitude = String.valueOf(aMapLocation.getLongitude());
			AppManager.setClientUser(clientUser);
			curLat = clientUser.latitude;
			curLon = clientUser.longitude;
			if (TextUtils.isEmpty(PreferencesUtils.getCurrentProvince(this))) {
				PreferencesUtils.setCurrentProvince(this, aMapLocation.getProvince());
			}
			uploadCityInfoRequest(aMapLocation.getCity(), String.valueOf(aMapLocation.getLatitude()),
					String.valueOf(aMapLocation.getLongitude()));
			PreferencesUtils.setLatitude(this, curLat);
			PreferencesUtils.setLongitude(this, curLon);
			if (!TextUtils.isEmpty(aMapLocation.getCity())) {
				stopLocation();
			}
		}

	}

	private void uploadCityInfoRequest(String city, String lat, String lon) {
		ArrayMap<String, String> params = new ArrayMap<>();
		params.put("channel", CheckUtil.getAppMetaData(CSApplication.getInstance(), "UMENG_CHANNEL"));
		params.put("currentCity", city);
		params.put("latitude", lat);
		params.put("longitude", lon);
		RetrofitFactory.getRetrofit().create(IUserApi.class)
				.uploadCityInfo(params, AppManager.getClientUser().sessionId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
				.subscribe(responseBody -> {} , throwable -> {});
	}

	/**
	 * 设置视图
	 */
	private void setupViews() {
		viewPager = findViewById(R.id.viewpager);
		viewPager.setNoScroll(true);
		bottomNavigationView = findViewById(R.id.bottom_navigation);
		//默认 >3 的选中效果会影响ViewPager的滑动切换时的效果，故利用反射去掉
		BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
		bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
			switch (item.getItemId()) {
				case R.id.item_news:
					viewPager.setCurrentItem(0);
					break;
				case R.id.item_lib:
					viewPager.setCurrentItem(1);
					break;
				case R.id.item_find:
					viewPager.setCurrentItem(2);
					break;
				case R.id.item_more:
					viewPager.setCurrentItem(3);
					break;
			}
			return false;
		});

		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				if(bottomNavigationView.getMenu().getItem(position).isChecked()){
					bottomNavigationView.getMenu().getItem(position).setChecked(false);
				}
			}

			@Override
			public void onPageSelected(int position) {
				bottomNavigationView.getMenu().getItem(position).setChecked(true);
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		setupViewPager(viewPager);

		BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
		if (menuView != null) {
			mQBadgeView = new QBadgeView(this);
			mBadgeView = mQBadgeView.setGravityOffset((float) (DensityUtil.getWidthInPx(this) / 3.2), 2, false)
					.bindTarget(menuView);
		}
	}

	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

		adapter.addFragment(new HomeLoveFragment());
		adapter.addFragment(new FoundFragment());
		adapter.addFragment(new MessageFragment());
		adapter.addFragment(new PersonalFragment());
		viewPager.setAdapter(adapter);
	}


	private void setupEvent(){
		MessageUnReadListener.getInstance().setMessageUnReadListener(this);
	}


	@Override
	public void notifyUnReadChanged(int type) {
		updateConversationUnRead();
	}

	/**
	 * 更新会话未读消息总数
	 */
	private void updateConversationUnRead() {
		if (mBadgeView != null) {
			int total = ConversationSqlManager.getInstance(this)
					.getAnalyticsUnReadConversation();
			if (total > 0) {
				mQBadgeView.setVisibility(View.VISIBLE);
				if (total >= 100) {
					mBadgeView.setBadgeText("99+");
				} else {
					mBadgeView.setBadgeText(String.valueOf(total));
				}
			} else {
				mQBadgeView.setVisibility(View.GONE);
			}
		}
	}

	private void requestLocationPermission() {
		if (!CheckUtil.isGetPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
				!CheckUtil.isGetPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
			if (rxPermissions == null) {
				rxPermissions = new RxPermissions(this);
			}
			rxPermissions.requestEachCombined(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
					.subscribe(permission -> {// will emit 1 Permission object
						if (permission.granted) {
							// All permissions are granted !
							startLocation();
						} else if (permission.shouldShowRequestPermissionRationale) {
							// At least one denied permission without ask never again
							if (!isSecondAccess) {
								showAccessLocationDialog();
							}
						} else {
							// At least one denied permission with ask never again
							// Need to go to the settings
							if (!isSecondAccess) {
								showAccessLocationDialog();
							}
						}
					}, throwable -> {

					});
		} else {
			startLocation();
		}
	}

	private void showAccessLocationDialog() {
		isSecondAccess = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.access_location);
		builder.setPositiveButton(R.string.ok, (dialog, i) -> {
			dialog.dismiss();
			Utils.goToSetting(MainActivity.this, REQUEST_LOCATION_PERMISSION);
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
	protected void onDestroy() {
		super.onDestroy();
		destroyLocation();
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
		if (requestCode == REQUEST_LOCATION_PERMISSION) {
			isSecondAccess = false;
			requestLocationPermission();
		}
	}

}
