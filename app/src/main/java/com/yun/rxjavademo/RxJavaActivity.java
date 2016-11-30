package com.yun.rxjavademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;

public class RxJavaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_java);
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            //Call会在被观察者发送时调用
            //我们在此方法中通过subscribe来发出
            //subscriber
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("hello RxJava");

            }
        });
        Observer<String> observer = new Observer<String>() {

            public static final String TAG = "yun";


            public void onCompleted() {

                Log.d(TAG, "onCompleted: ");
            }


            public void onError(Throwable e) {

                Log.e(TAG, "onError: ",e);
            }
            public void onNext(String s) {
                Log.i(TAG, "onNext: "+s);
            }
        };
        //观察者
        Subscriber<String> stringSubscriber = new Subscriber<String>() {

            public static final String TAG = "yun";

            @Override
            public void onCompleted() {

                Log.d(TAG, "onCompleted: ");
            }

            @Override
            public void onError(Throwable e) {

                Log.e(TAG, "onError: ",e);
            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "onNext: "+s);
            }
        };
        //通过subscribe函数
        observable.subscribe(stringSubscriber);
    }
}
