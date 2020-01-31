package com.karl.fingerprintmodule.Models

import java.io.Serializable

data class User(
    var id: String,
    var f_name: String,
    var l_name: String,
    var image_path: String
)
    : Serializable