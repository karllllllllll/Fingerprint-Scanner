package com.karl.fingerprintmodule

import com.digitalpersona.uareu.Fmd
import java.io.Serializable

class userBiometrix(
    var name: String,

    @Transient
    var fingerPrint: Fmd
) : Serializable