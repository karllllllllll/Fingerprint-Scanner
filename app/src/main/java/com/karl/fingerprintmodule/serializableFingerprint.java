package com.karl.fingerprintmodule;

import com.digitalpersona.uareu.Fmd;

import java.io.Serializable;

public class serializableFingerprint implements Serializable {

    //private static final long serialVersionUID = -7927942469935489216L;
    Fmd fingerPrint;

    public serializableFingerprint(Fmd f) {
        this.fingerPrint = f;
    }

    public Fmd getFingerPrint() {
        return fingerPrint;
    }
}
