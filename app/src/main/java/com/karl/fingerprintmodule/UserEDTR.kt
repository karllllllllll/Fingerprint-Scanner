package com.karl.fingerprintmodule

data class UserEDTR(
    var user_id: String,
    var time: String,
    var reference: String,
    var pin: String,
    var date: String,
    var location_id: String
)