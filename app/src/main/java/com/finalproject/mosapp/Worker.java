package com.finalproject.mosapp;

class Worker {
    MyCallback callback;

    void onEvent() {
        callback.callbackCall();
    }
}