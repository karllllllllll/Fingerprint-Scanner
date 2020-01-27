package com.karl.fingerprintmodule

data class fingerprint(

    var userID: String,
    var data: String,
    var width: Int,
    var height: Int,
    var resolution: Int,
    var cbeff_id: Int
)