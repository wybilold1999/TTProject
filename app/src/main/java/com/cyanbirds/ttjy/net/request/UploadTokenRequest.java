package com.cyanbirds.ttjy.net.request;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.cyanbirds.ttjy.CSApplication;
import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.net.base.ResultPostExecute;
import com.cyanbirds.ttjy.utils.PreferencesUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author Cloudsoar(wangyb)
 * @datetime 2016-05-03 10:38 GMT+8
 * @email 395044952@qq.com
 */
public class UploadTokenRequest extends ResultPostExecute<String> {
    public void request(String gtClientId, String xgToken){
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put("gtClientId", gtClientId);
        map.put("xgToken", xgToken);
        Call<ResponseBody> call = AppManager.getUserService().uploadToken(map, AppManager.getClientUser().sessionId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.body() != null) {
                    response.body().close();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                onErrorExecute(CSApplication.getInstance()
                        .getResources()
                        .getString(R.string.network_requests_error));
            }
        });
    }

}
