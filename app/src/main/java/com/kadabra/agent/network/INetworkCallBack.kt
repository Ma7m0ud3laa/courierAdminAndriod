package com.twoam.Networking


interface INetworkCallBack <U>{

    fun onSuccess(response:U)

    fun onFailed(error: String)

}
