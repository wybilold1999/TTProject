package com.cyanbirds.ttjy.net.request;

import android.text.TextUtils;

import com.cyanbirds.ttjy.config.AppConstants;
import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.net.base.ResultPostExecute;
import com.cyanbirds.ttjy.utils.AESOperator;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by wangyb on 2017/5/17.
 * 描述：获取微信id
 */

public class GetWeChatIdRequest extends ResultPostExecute<String> {

    public void request(String pay) {
        Call<ResponseBody> call = AppManager.getUserService().getWeChatId(pay);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        parseJson(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        onErrorExecute("");
                    } finally {
                        response.body().close();
                    }
                } else {
                    onErrorExecute("");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                onErrorExecute("");
            }
        });
    }

    private void parseJson(String json){
        try {
            String decryptData = AESOperator.getInstance().decrypt(json);
            if (!TextUtils.isEmpty(decryptData)) {
                onPostExecute(decryptData);
            } else {
                onErrorExecute("");
            }
        } catch (Exception e) {
            onErrorExecute("");
        }
    }

}
