package com.twoam.agent.utilities

import com.twoam.agent.model.Stop
import com.twoam.agent.model.Ticket
import com.twoam.agent.model.User


/**
 * Created by Mokhtar on 6/18/2019.
 */
object AppConstants {


    const val SERVER_IP = "165.227.135.161"
    const val BASE_URL = "http://$SERVER_IP/trolley/public/api/"
    const val APP_DATABASE_NAME = "appdb"
    // region BASIC RESPONSE
    val STATUS_CODE = "status_code"
    val MESSAGE = "message"
    val DATA = "data"
    //endregion
    // region CODE RESPONSE
    val CODE_204 = 204
    val CODE_201 = 201
    val CODE_200 = 200
    val CODE_444 = 444
    //endregion
    //region languages
    val LANGUAGE = "lang"
    val ARABIC = "ar"
    val ENGLISH = "en"
    val ALERT = "Alert"
    val ERROR = "Error"
    val INFO = "Info"
    val WARNING = "Warning"
    val OK = "ok"
    val CANCEL = "cancel"

    val IS_FIRST = "is_first"
    val IS_LOGIN = "login"


    //endregion
    //region TOKEN
    var token = ""
    //endregion
    //region Auth
    const val SIGN_UP = "signup"
    const val URL_SIGN_UP = "customer/auth/signup"


    const val DOMAIN = "trolleyDomain"
    const val URL_LOGIN = "customer/auth"
    const val URL_LOG_OUT = "customer/auth/logout"
    const val URL_GUEST_LOGIN = "customer/auth/guest"
    const val URL_FORGET_PASSWORD = "customer/auth/forget_password"
    const val URL_VALODATE_REQUET_CODE = "customer/auth/validate_code"
    const val URL_RESET_PASSWORD = "customer/auth/reset_password"

    const val URL_SOCIAL_AUTH_FACEBOOK = "customer/auth/social/facebook"
    const val URL_SOCIAL_AUTH_GOOGLE = "customer/auth/social/google"

    const val URL_SOCIAL_SIGN_UP = "customer/auth/social_signup"
    const val URL_ADD_ADDRESS = "customer/profile/addresses"
    const val URL_UPDATE_ADDRESS = "customer/profile/addresses"
    const val URL_REMOVE_ADDRESS = "customer/profile/addresses/remove"
    const val URL_EDIT_PROFILE = "customer/profile/edit"
    const val URL_CHANGE_PASSWORD = "customer/profile/change_password"
    const val URL_ADD_TOKEN = "customer/profile/add_token"
    const val URL_GET_CITIES = "cities"
    const val URL_GET_CATEGORIES = "categories"
    const val URL_GET_DEPARTMENT = "customer/departments"
    const val URL_GET_DEPARTMENT_CATEGORIES = "customer/departments/categories"
    const val URL_GET_ADS = "customer/ads"
    const val URL_GET_SUB_CATEGORIES = "customer/categories/products"
    const val URL_GET_HOME_PRODUCTS = "customer/products/home"
    const val URL_GET_PRODUCT_DETAILS = "customer/products"
    const val URL_PRODUCT_FAVOURITES = "customer/products/favourites"
    const val URL_PRODUCT_ADD_TO_FAVOURITE = "customer/products/favourite"
    const val URL_PRODUCT_REMOVE_FROM_FAVOURITE = "customer/products/unfavourite"

    const val URL_GET_MEDICAL_PRESCRIPTIONS_GET_ALL = "customer/prescriptions"
    const val URL_GET_MEDICAL_ADD = "customer/prescriptions"
    const val URL_GET_ORDERS = "customer/orders"
    const val URL_CANCEL_ORDERS = "customer/orders/cancel"
    const val URL_ORDERS_CHECK_PROMO = "customer/orders/check_promo"
    const val URL_ORDERS_CREATE = "customer/orders"
    const val URL_PRODUCTS_SEARCH = "customer/products/search"
    const val URL_PRODUCTS_FILTER = "customer/products/filter"


    //endregion

    //region  app variables


    const val TEST_MODE = "testMode"

    const val ADDRESS = "address"
    const val HASADDRESS = "hasAddress"
    const val TRUE = "true"
    const val FALSE = "false"
    const val USER_DATA = "userData"
    const val CITIES_DATA = "citiesData"
    const val CART_ITEMS = "CartItems"
    const val CHANGE_PASSWORD = "changePassword"

    const val FACEBOOK = 1
    const val GOOGLE = 2
    const val CURRENTINDEXTAG = "currentOption"
    const val FAVOURITEPRODUCTS = "favouriteData"
    const val SEARCH_DATA = "searchData"
    const val SEARCH_SUB_CATEGORY = "searchSubCategoryData"
    var CURRENTSELECTEDINDEX = 0
    var NOTIFICATION = "notification"
    var CurrentCameraGAlleryAction = 0 //0 camera  1 gallery
    var CurrentLoginUser: User = User()
    var currentSelectedTicket: Ticket = Ticket()

    var CurrentSelectedStop: Stop = Stop()


    const val BEARER = "Bearer "

    //facebook social login
    const val EMAIL = "email"
    const val PUBLIC_PROFILE = "public_profile"
    const val USER_PERMISSION = "user_friends"
    //endregion

    //region bottom sheet
    var isClosed = false
    //endregion
    //region others
    const val TOP_PROMOTIONS = "Top Pormotions"
    const val MOST_SELLING = "Most Selling"

    //endregion

}