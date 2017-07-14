/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wychoi.success.memoalarm.memo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.wychoi.success.memoalarm.BaseFragment;
import com.wychoi.success.memoalarm.R;
import com.wychoi.success.memoalarm.observablescrollview.ObservableListView;
import com.wychoi.success.memoalarm.observablescrollview.ObservableScrollViewCallbacks;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.wychoi.success.memoalarm.MainActivity.networkService;


public class MemoListViewFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.memo_fragment_listview, container, false);

        Activity parentActivity = getActivity();
        final ObservableListView listView = (ObservableListView) view.findViewById(R.id.scroll);
        listView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.container));
        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            listView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }

        //GET
        Call<ResponseBody> memoListCall = networkService.get_memo_list();
        memoListCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Log.d("wychoi","response.isSuccessful() :"+response.isSuccessful());
                    try {
                        Log.d("wychoi","response.raw() :"+response.raw());
                        String responseBodyString = response.body().string();
                        Log.d("wychoi","response.body().string(); :"+responseBodyString);

                        //JsonString을 Json객체로 parsing

                        JsonParser parser = new JsonParser();
                        JsonArray memoJsonArrayList = (JsonArray) parser.parse(responseBodyString);
                        ArrayList<MemoVO> memoList= new ArrayList<MemoVO>();
                        for(JsonElement memo : memoJsonArrayList){
                            Log.d("wychoi","memo :"+memo.toString());
                            MemoVO memoVO =  new MemoVO();
                            memoVO.jsonObjectToMemoVO(memo.getAsJsonObject());
                            memoList.add(memoVO);
                        }

                        listView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, memoList));
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

        return view;
    }

    @Override
    public void onFabClick() {
        Log.d("wychoi","MemoListViewFragment"); // OK
        //여기에 알람 생성하는 매소드 만들어서 넣자!
        //mTimePickerDialogController.show(0, 0, makeTimePickerDialogTag());
    }


}
