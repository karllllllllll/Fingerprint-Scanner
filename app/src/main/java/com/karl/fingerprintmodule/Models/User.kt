package com.karl.fingerprintmodule.Models

import java.io.Serializable

//data class User(
//    var id: String,
//    var f_name: String,
//    var l_name: String,
//    var image_path: String
//)
//    : Serializable

data class User(
    var id: String,
    var f_name: String,
    var l_name: String,
    var image_path: String,


    var pin: String,
    var api_token: String,
    var link: String,

    var time_in:String,
    var time_out:String,
    var date:String

) : Serializable