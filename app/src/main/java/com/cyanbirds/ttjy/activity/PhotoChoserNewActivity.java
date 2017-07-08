package com.cyanbirds.ttjy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.base.BaseActivity;
import com.cyanbirds.ttjy.adapter.PhotoChoserAdapter;
import com.cyanbirds.ttjy.config.AppConstants;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.entity.DynamicContent;
import com.cyanbirds.ttjy.entity.ImageBean;
import com.cyanbirds.ttjy.entity.Picture;
import com.cyanbirds.ttjy.eventtype.PubDycEvent;
import com.cyanbirds.ttjy.listener.ChoseImageListener;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.net.ImagesLoader;
import com.cyanbirds.ttjy.net.request.OSSImagUploadRequest;
import com.cyanbirds.ttjy.net.request.PublishDynamicRequest;
import com.cyanbirds.ttjy.utils.FileAccessorUtils;
import com.cyanbirds.ttjy.utils.ImageUtil;
import com.cyanbirds.ttjy.utils.ProgressDialogUtils;
import com.cyanbirds.ttjy.utils.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @Description:图片选择
 * @author wangyb
 * @Date:2015年7月27日下午3:28:06
 */
public class PhotoChoserNewActivity extends BaseActivity implements
		LoaderCallbacks<List<ImageBean>>, ChoseImageListener{

	private TextView mSelectNumber;
	private RecyclerView mRecyclerView;
	private PhotoChoserAdapter mAdapter;

	private List<ImageBean> mImages;
	private int mSelectedCount = 0;

	private static final int MAX_SELECT_NUMBER = 3;

	/**
	 * 已经选中的url
	 */
	private List<String> imgUrls = null;
	/**
	 * 选中本地图片的url
	 */
	List<String> list = null;
	/**
	 * oss上面的url
	 */
	ArrayList<Picture> ossImgUrls = null;
	private int count = 0;//OSS上传的时候修改piclist中的url值
	private Gson gson;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_choser);
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}
		setupViews();
		setupEvent();
		setupData();
	}

	/**
	 * 设置视图
	 */
	private void setupViews() {
		mSelectNumber = (TextView) findViewById(R.id.select_number);
		mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
		mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
	}

	private void setupEvent(){
	}

	/**
	 * 设置数据
	 */
	private void setupData() {
		gson = new Gson();
		mImages = new ArrayList<ImageBean>();
		mAdapter = new PhotoChoserAdapter(this, mImages);
		mAdapter.setChoseImageListener(this);
		mRecyclerView.setAdapter(mAdapter);
		getSupportLoaderManager().initLoader(0, null, this);
		imgUrls = getIntent().getStringArrayListExtra(ValueKey.IMAGE_URL);
		if (imgUrls != null && imgUrls.size() > 0) {
			mSelectedCount = imgUrls.size();
			refreshPreviewTextView();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.define_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.ok) {
			list = getSelectedImagePaths();
			if(list != null && list.size() > 0){
				ossImgUrls = new ArrayList<>();
				ProgressDialogUtils.getInstance(PhotoChoserNewActivity.this).show(R.string.dialog_request_uploda);
				for (String path : list) {
					ossImgUrls.add(ImageUtil.getPicInfoForPath(path));
					String imgUrl = ImageUtil.compressImage(path, FileAccessorUtils.IMESSAGE_IMAGE);
					new OSSUploadImgTask().request(AppManager.getFederationToken().bucketName,
							AppManager.getOSSFacePath(), imgUrl);
				}
			}
		}
		return super.onOptionsItemSelected(item);
	}

	class OSSUploadImgTask extends OSSImagUploadRequest {
		@Override
		public void onPostExecute(String s) {
			count++;
			if (count <= ossImgUrls.size()) {
				ossImgUrls.get(count - 1).path = AppConstants.OSS_IMG_ENDPOINT + s;
				if (count == list.size()) {
					String picUrls = gson.toJson(ossImgUrls);
					new PublishDynamicTask().request(picUrls, "分享图片");
				}
			}
		}

		@Override
		public void onErrorExecute(String error) {
			ProgressDialogUtils.getInstance(PhotoChoserNewActivity.this).dismiss();
		}
	}

	class PublishDynamicTask extends PublishDynamicRequest {
		@Override
		public void onPostExecute(String s) {
			JsonObject obj = new JsonParser().parse(s).getAsJsonObject();
			JsonObject data = obj.get("data").getAsJsonObject();
			Object o = data.get("pictures");
			if (!(o instanceof JsonNull)) {
				Type listType = new TypeToken<ArrayList<DynamicContent.DataBean.PicturesBean>>() {
				}.getType();
				ArrayList<DynamicContent.DataBean.PicturesBean> picturesBeen = gson.fromJson(
						data.get("pictures").getAsJsonArray(), listType);
				ArrayList<String> list = new ArrayList<>();
				for (DynamicContent.DataBean.PicturesBean pb : picturesBeen) {
					list.add(pb.getPath());
				}
				ProgressDialogUtils.getInstance(PhotoChoserNewActivity.this).dismiss();
				ToastUtil.showMessage(R.string.upload_success);
				Intent intent = new Intent();
				intent.putExtra(ValueKey.IMAGE_URL, list);
				setResult(RESULT_OK, intent);
				finish();
			}
		}

		@Override
		public void onErrorExecute(String error) {
			ProgressDialogUtils.getInstance(PhotoChoserNewActivity.this).dismiss();
		}
	}

	/**
	 * 刷新预览
	 */
	private void refreshPreviewTextView() {
		if (mSelectedCount > 0) {
			mSelectNumber.setText(String.valueOf(mSelectedCount));
			mSelectNumber.setEnabled(true);
		} else {
			mSelectNumber.setText(String.valueOf(0));
			mSelectNumber.setEnabled(false);
		}
	}

	/**
	 * 获取选择的图片路径
	 * 
	 * @return
	 */
	private List<String> getSelectedImagePaths() {
		List<String> selectedImages = new ArrayList<String>();
		for (ImageBean image : mImages) {
			if (image.isSeleted()) {
				selectedImages.add(image.getPath());
			}
		}
		return selectedImages;
	}

	@Override
	public Loader<List<ImageBean>> onCreateLoader(int arg0, Bundle arg1) {
		return new ImagesLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<List<ImageBean>> loader,
			List<ImageBean> imageBeans) {
		this.mImages.clear();
		this.mImages.addAll(imageBeans);
		if (imgUrls != null && imgUrls.size() > 0) {
			for (int i = 0; i < imageBeans.size(); i++) {
				if (imgUrls.contains(imageBeans.get(i).getPath())) {
					imageBeans.get(i).setSeleted(true);
				}
			}
		}
		mAdapter.notifyDataSetChanged();

	}

	@Override
	public void onLoaderReset(Loader<List<ImageBean>> arg0) {

	}

	@Override
	public boolean onSelected(ImageBean image) {
		if (mSelectedCount >= MAX_SELECT_NUMBER) {
			ToastUtil.showMessage(R.string.arrive_limit_count);
			return false;
		}
		image.setSeleted(true);
		mSelectedCount++;
		refreshPreviewTextView();
		return true;
	}

	@Override
	public boolean onCancelSelect(ImageBean image) {
		image.setSeleted(false);
		mSelectedCount--;
		refreshPreviewTextView();
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
}
