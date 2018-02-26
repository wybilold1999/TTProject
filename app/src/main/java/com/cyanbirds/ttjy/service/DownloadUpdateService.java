package com.cyanbirds.ttjy.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.entity.IMessage;
import com.cyanbirds.ttjy.listener.FileProgressListener;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.net.request.DownloadApkFileRequest;
import com.cyanbirds.ttjy.utils.FileAccessorUtils;
import com.cyanbirds.ttjy.utils.ToastUtil;

import java.io.File;

/**
 * 作者：wangyb
 * 时间：2016/9/24 17:42
 * 描述：
 */
public class DownloadUpdateService extends Service implements FileProgressListener.OnFileProgressChangedListener {

	private NotificationManager manager = null;
	private NotificationCompat.Builder builder = null;
	private Notification notification = null;

	private String apkUrl = null;

	public DownloadUpdateService() {
		/*manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		builder = new NotificationCompat.Builder(DownloadUpdateService.this);
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setContentTitle(getString(R.string.app_name));
		builder.setTicker(getString(R.string.downloading));
		builder.setContentText(String.format(getString(R.string.download_progress), 0f));

		notification = builder.build();
		notification.vibrate = new long[]{500, 500};
		notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
		manager.notify(0, notification);*/
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		FileProgressListener.getInstance().addOnFileProgressChangedListener(this);
		manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		builder = new NotificationCompat.Builder(DownloadUpdateService.this);
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setContentTitle(getString(R.string.app_name));
		builder.setTicker(getString(R.string.downloading));
		builder.setContentText(String.format(getString(R.string.download_progress), 0f));

		notification = builder.build();
//		notification.vibrate = new long[]{500, 500};
//		notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
		notification.defaults = Notification.DEFAULT_SOUND;
		manager.notify(0, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		apkUrl = intent.getStringExtra(ValueKey.APK_URL);
		File apkFile = new File(FileAccessorUtils.APK_PATH, "ttjy.apk");
		if (apkFile.exists()) {
			apkFile.delete();
		}
		new DownloadApkFileTask().request(apkUrl, FileAccessorUtils.APK_PATH, "ttjy.apk");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onFileProgressChanged(IMessage message, int progress) {
		float fprogress = progress;
		builder.setContentText(String.format(getString(R.string.download_progress), fprogress));
		builder.setProgress(100, progress, false);
		manager.notify(0, builder.build());
	}

	class DownloadApkFileTask extends DownloadApkFileRequest {
		@Override
		public void onPostExecute(String s) {
			FileProgressListener.getInstance().removeOnFileProgressChangedListener(DownloadUpdateService.this);
			File apkFile = new File(s);

			/**
			 * 创建通知栏的intent
			 */
			Intent notifyIntent = new Intent(Intent.ACTION_VIEW);
			Uri uri = Uri.fromFile(apkFile);
			//设置intent的类型
			notifyIntent.setDataAndType(uri,
					"application/vnd.android.package-archive");
			PendingIntent pendingIntent = PendingIntent.getActivity(DownloadUpdateService.this, 0, notifyIntent, 0);
			builder.setContentIntent(pendingIntent);
			builder.setContentText(getString(R.string.download_finish));
			builder.setProgress(100, 100, false);
			manager.notify(0, builder.build());

			/**
			 * 弹出app的安装界面
			 */
			AppManager.installApk(apkFile);

			stopSelf();
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
			builder.setContentText(getString(R.string.download_fail));
			manager.notify(0, builder.build());
		}
	}
}
