package com.cyanbirds.ttjy.presenter;

import android.support.v4.util.ArrayMap;

import com.cyanbirds.ttjy.manager.AppManager;
import com.cyanbirds.ttjy.net.IUserApi;
import com.cyanbirds.ttjy.net.base.RetrofitFactory;
import com.cyanbirds.ttjy.view.IUserLoginLogOut;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SmsCodePresenterImpl implements IUserLoginLogOut.CheckSmsCodePresenter {

    private IUserLoginLogOut.CheckSmsCodeView checkSmsCodeView;

    public SmsCodePresenterImpl(IUserLoginLogOut.CheckSmsCodeView view) {
        this.checkSmsCodeView = view;
    }

    @Override
    public void checkSmsCode(String code, String phoneNum, int mPhoneType) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("phone", phoneNum);
        params.put("zone", "86");
        params.put("code", code);
        params.put("type", String.valueOf(mPhoneType)); //0:注册  1:找回密码
        params.put("device", "android");
        RetrofitFactory.getRetrofit().create(IUserApi.class)
                .checkSmsCode(AppManager.getClientUser().sessionId, params)
                .subscribeOn(Schedulers.io())
                .map(responseBody -> {
                    JsonObject obj = new JsonParser().parse(responseBody.string()).getAsJsonObject();
                    return obj.get("status").getAsInt();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(checkSmsCodeView::checkSmsCode, throwable -> checkSmsCodeView.onShowNetError());
    }
}
