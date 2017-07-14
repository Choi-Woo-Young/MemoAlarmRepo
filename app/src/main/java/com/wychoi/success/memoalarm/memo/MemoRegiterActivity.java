package com.wychoi.success.memoalarm.memo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;

import com.wychoi.success.memoalarm.R;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.wychoi.success.memoalarm.MainActivity.networkService;

public class MemoRegiterActivity extends AppCompatActivity {

    //메모 내용
    MultiAutoCompleteTextView textView_contents;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_regiter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //메모 내용
        textView_contents = (MultiAutoCompleteTextView) findViewById(R.id.TextView_contents);
        //등록 버튼
        Button button_memo_reg = (Button) findViewById(R.id.button_memo_reg);
        button_memo_reg.setOnClickListener(mClickListener);
    }


    //메모 등록 버튼 클릭 이벤트
    Button.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            //이곳에 버튼 클릭시 일어날 일을 적습니다.

            //textView_contents

            MemoVO memoVO = new MemoVO();
            memoVO.setContents(textView_contents.getText().toString());

            Log.d("wychoi","memoVO: "+memoVO.toString());
            //GET
            Call<ResponseBody> insertMemoCall = networkService.insert_memo(memoVO);
            insertMemoCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()){
                        Log.d("wychoi","response.isSuccessful() :"+response.isSuccessful());
                        try {
                            Log.d("wychoi","response.raw() :"+response.raw());
                            String responseBodyString = response.body().string();
                            Log.d("wychoi","response.body().string(); :"+responseBodyString);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Log.d("wychoi","response.isSuccessful() :"+response.isSuccessful());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("wychoi","onFailure"); // OK
                }
            });

        }
    };



}
