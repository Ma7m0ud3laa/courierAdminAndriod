package com.reach.plus.admin.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.twoam.agent.model.User
import com.twoam.agent.utilities.AppConstants



class UserSessionManager(val context: Context) {
    private val editor: SharedPreferences.Editor
    private val sharedPreferences: SharedPreferences
    private val sharedPrefName = UserSessionManager::class.java.name + "_shared_preferences"

    init {
        sharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.apply()
    }

    companion object {
        var sUserSessionManager: UserSessionManager? = null
        private val USER_OBJECT = UserSessionManager::class.java.name + "_user_object"


        @Synchronized
        fun getInstance(context: Context): UserSessionManager {
            if (sUserSessionManager == null) {
                sUserSessionManager = UserSessionManager(context)
            }
            return sUserSessionManager as UserSessionManager
        }
    }

    fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean(AppConstants.IS_FIRST, true)
    }

    fun setFirstTime(isFirst: Boolean) {
        editor.putBoolean(AppConstants.IS_FIRST, isFirst).commit()
    }

    fun isLogined(): Boolean {
        return sharedPreferences.getBoolean(AppConstants.IS_LOGIN, false)
    }

    fun setIsLogined(isFirst: Boolean) {
        editor.putBoolean(AppConstants.IS_FIRST, isFirst).commit()
    }

    fun getLanguage(): String {
        return sharedPreferences.getString(AppConstants.LANGUAGE, AppConstants.ENGLISH)
    }

    fun setLanguage(language: String) {
        editor.putString(AppConstants.LANGUAGE, language).commit()
    }


    fun setUserData(userModel: User?) {
        val gson = Gson()
        val json = gson.toJson(userModel)
        editor.putString(USER_OBJECT, json)
        editor.commit()
    }

    fun getUserData(): User? {
        val gson = Gson()
        val json = sharedPreferences.getString(USER_OBJECT, "")
        var userModel: User? = gson.fromJson<Any>(json, User::class.java) as User?
        return userModel
    }

    fun logout() {
        var isFirst = isFirstTime()
        editor.clear()
        editor.commit()
        setFirstTime(isFirst)
        setIsLogined(false)
    }


}
