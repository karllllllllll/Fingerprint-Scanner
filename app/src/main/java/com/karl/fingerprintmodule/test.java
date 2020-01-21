package com.karl.fingerprintmodule;

import java.io.Serializable;
import kotlin.jvm.Transient;

public class test implements Serializable {

    @Transient
    String message = "test";
}

