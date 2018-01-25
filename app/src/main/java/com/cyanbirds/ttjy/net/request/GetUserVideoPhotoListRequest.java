package com.cyanbirds.ttjy.net.request;

import com.cyanbirds.ttjy.CSApplication;
import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.entity.UserVideoPhotoModel;
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
 * 获取用户信息、视频信息、相册信息
 */
public class GetUserVideoPhotoListRequest extends ResultPostExecute<UserVideoPhotoModel> {
    public void request(String userId) {
        Call<ResponseBody> call = AppManager.getUserService().getUserVideoPhotoList(userId, AppManager.getClientUser().sessionId);
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
            String data = obj.get("data").getAsString();
            Gson gson = new Gson();
            UserVideoPhotoModel model = gson.fromJson(data, UserVideoPhotoModel.class);
            onPostExecute(model);
        } catch (Exception e) {
            onErrorExecute(CSApplication.getInstance().getResources()
                    .getString(R.string.data_parser_error));
        }
    }
}
