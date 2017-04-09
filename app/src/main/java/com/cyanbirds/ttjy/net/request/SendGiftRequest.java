package com.cyanbirds.ttjy.net.request;

import android.support.v4.util.ArrayMap;

import com.cyanbirds.ttjy.CSApplication;
import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.entity.UserVipModel;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.net.base.ResultPostExecute;
import com.cyanbirds.ttjy.utils.AESOperator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Administrator on 2016/5/1.
 */
public class SendGiftRequest extends ResultPostExecute<String> {
    public void request(String uid, String giftUrl, String goldNum) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("uid", uid);
        params.put("giftUrl", giftUrl);
        params.put("goldNum", goldNum);
        Call<ResponseBody> call = AppManager.getUserService().sendGift(params, AppManager.getClientUser().sessionId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        parseJson(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        response.body().close();
                    }
                } else {
                    onErrorExecute(CSApplication.getInstance()
                            .getResources()
                            .getString(R.string.network_requests_error));
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

    private void parseJson(String json) {
        try {
            String decryptData = AESOperator.getInstance().decrypt(json);
            JsonObject obj = new JsonParser().parse(decryptData).getAsJsonObject();
            int code = obj.get("code").getAsInt();
            if (code != 0) {
                onErrorExecute(CSApplication.getInstance().getResources()
                        .getString(R.string.data_load_error));
                return;
            }
            onPostExecute("");
        } catch (Exception e) {
            onErrorExecute(CSApplication.getInstance().getResources()
                    .getString(R.string.data_parser_error));
        }
    }
}
