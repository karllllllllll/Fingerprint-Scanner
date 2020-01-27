package com.karl.fingerprintmodule.ViewModels;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.karl.fingerprintmodule.Globals;
import com.karl.fingerprintmodule.Result;

import org.jetbrains.annotations.NotNull;

public class MainActivityViewModel extends AndroidViewModel {

    private Application app;

    private MutableLiveData<ReaderCollection> readers = new MutableLiveData<>();

    public MutableLiveData<ReaderCollection> getReaders() {
        return this.readers;
    }

    private MutableLiveData<com.karl.fingerprintmodule.Result> Result = new MutableLiveData<>();

    public MutableLiveData<Result> getResult() {
        return this.Result;
    }

    public MainActivityViewModel(@NonNull Application application) {
        super(application);

        this.app = application;
    }

    public void getReader() {

        try {
            readers.setValue(Globals.getInstance().getReaders(app.getBaseContext()));
        } catch (UareUException e) {
            readers.setValue(null);
        }
    }
}
