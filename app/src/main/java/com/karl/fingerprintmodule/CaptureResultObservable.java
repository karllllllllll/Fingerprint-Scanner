package com.karl.fingerprintmodule;

import com.digitalpersona.uareu.Reader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class CaptureResultObservable {
    private static Reader.CaptureResult captureResult;
    private ChangeListener listener;


    public void setCaptureResult(Reader.CaptureResult cr) {
        captureResult = cr;
        if (listener != null) listener.onChange();
    }

    public Reader.CaptureResult getCaptureResult(){
        return captureResult;
    }

    public ChangeListener getListener() {
        return listener;
    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void onChange();
    }
}
