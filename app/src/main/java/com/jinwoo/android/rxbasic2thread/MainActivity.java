package com.jinwoo.android.rxbasic2thread;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public final static String TAG = "PINGPONG";
    TextView textView1, textView2, textView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);

        // 실제 Task를 처리하는 객체(발행자)
        Observable<String> simpleObservable =
                Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        // 네트웍을 통해서 데이터를 긁어온다.
                        // 반복문을 돌면서 ----------------------------------
                        for (int i = 0; i<10; i++) {
                            subscriber.onNext("Hello RxAndroid !" + i); // onNext로 구독자들에게 데이터를 보내준다.
                            try{
                                Thread.sleep(2000);
                            } catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                        // ----------------------------------
                        subscriber.onCompleted();
                    }
                });

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        // 기본형 - thread를 지정해준다.
        simpleObservable
                .subscribeOn(Schedulers.io())   // 발행자를 별도의 io thread에서 동작 시킨다.
                .observeOn(AndroidSchedulers.mainThread())  // 구독자를 mainThread에서 동작시킨다.
                .subscribe(new Subscriber<String>() { // Observer(구독자)
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "[Observer1] complete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "[Observer1] error" + e.getMessage());
                    }

                    @Override
                    public void onNext(String text) {
                        textView1.setText("[Observer1]" + text);
                    }
                });

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        // 옵저버(구독자)를 등록하는 함수 - 진화형 (각 함수를 하나의 콜백객체에 나눠서 담아준다.)
        simpleObservable
                .subscribeOn(Schedulers.io())   // 발행자를 별도의 thread에서 동작 시킨다.
                .observeOn(AndroidSchedulers.mainThread())  // 구독자를 mainThread에서 동작시킨다.
                .subscribe(new Action1<String>() {    // onNext 함수와 동일한 역할을 하는 콜백객체
            @Override
            public void call(String s) {
                //Toast.makeText(MainActivity.this, "[Observer2]" + s, Toast.LENGTH_SHORT).show();
                textView2.setText("[Observer2]" + s);

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {         // onError 함수와 동일한 역할을 하는 콜백 객체
                Log.e(TAG, "[Observer2] error" + throwable.getMessage());

            }
        }, new Action0() {
            @Override
            public void call() {                            // onComplete과 동일한 역할을 하는 콜백 객체
                Log.d(TAG, "[Observer2] complete");

            }
        });


        try{
            Thread.sleep(2000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        // 옵저버(구독자)를 등록하는 함수 - 최종진화형 (람다식)
        simpleObservable
                .subscribeOn(Schedulers.io())   // 발행자를 별도의 io thread에서 동작 시킨다.
                .observeOn(AndroidSchedulers.mainThread())  // 구독자를 mainThread에서 동작시킨다.
                .subscribe(
                (string) -> {  textView3.setText("[Observer3]" + string); }
                    //Toast.makeText(MainActivity.this, "[Observer3]" + string, Toast.LENGTH_SHORT).show();}
                ,(error) -> {Log.e(TAG, "[Observer3] error" + error.getMessage());}
                ,() -> {Log.d(TAG, "[Observer3] complete");}
        );

    }
}
