package com.karl.fingerprintmodule
import java.io.Serializable

data class UserEDTR(
    var date_in: String,
    var time_in: String,
    var time_out: String
) : Serializable