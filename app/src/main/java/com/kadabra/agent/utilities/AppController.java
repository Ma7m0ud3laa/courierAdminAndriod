package com.kadabra.agent.utilities;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.reach.plus.admin.util.UserSessionManager;
import com.kadabra.agent.R;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class AppController extends Application {
    private static AppController mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mContext = this;
        if (UserSessionManager.Companion.getInstance(this).getLanguage() == AppConstants.INSTANCE.getENGLISH())
            LanguageUtil.changeLanguageType(mContext, new Locale(AppConstants.INSTANCE.getENGLISH()));
        else
            LanguageUtil.changeLanguageType(mContext, new Locale(AppConstants.INSTANCE.getARABIC()));

//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("font/avenir_book.ttf")
//                .setFontAttrId(R.attr.fontPath)
//                .build()
//        );
    }


    public static synchronized AppController getInstance() {
        return mContext;
    }

    public static Context getContext() {
        return mContext;
    }


}
