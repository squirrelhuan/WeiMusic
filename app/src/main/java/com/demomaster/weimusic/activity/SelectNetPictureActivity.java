package com.demomaster.weimusic.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.RetrofitInterface;
import com.demomaster.weimusic.model.MusicResponse;
import com.demomaster.weimusic.model.NetImage;
import com.demomaster.weimusic.ui.adapter.MusicRecycleViewAdapter;
import com.demomaster.weimusic.ui.adapter.MusicRecycleViewAdapter2;
import com.demomaster.weimusic.ui.adapter.NetPictureAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.http.HttpUtils;
import cn.demomaster.qdlogger_library.QDLogger;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class SelectNetPictureActivity extends BaseActivity {

    RecyclerView pic_recyclerview;
    NetPictureAdapter adapter;
    List<MusicResponse.ResultDTO.SongsDTO> netImageList= new ArrayList<>();
    EditText et_input;
    Button btn_search;
    @Override
    public View getHeaderlayout() {
        return View.inflate(this, R.layout.activity_actionbar_search,null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_net_picture);

        et_input = findViewById(R.id.et_input);
        btn_search = findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(et_input.getText())) {
                    getData(et_input.getText().toString());
                }
            }
        });
        pic_recyclerview = findViewById(R.id.pic_recyclerview);
        adapter = new NetPictureAdapter(mContext,netImageList);
        //这里使用线性布局像listview那样展示列表,第二个参数可以改为 HORIZONTAL实现水平展示
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        //使用网格布局展示
        pic_recyclerview.setLayoutManager(new GridLayoutManager(mContext, 2));
        //recy_drag.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        //设置分割线使用的divider
        //rv_songs.addItemDecoration(dividerItemDecoration);
        pic_recyclerview.setAdapter(adapter);
        adapter.setOnItemClickListener(new MusicRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putString("PicUrl",netImageList.get(position).getAlbum().getPicUrl());
                intent.putExtras(bundle);
                setResult(RESULT_OK,intent);
                finish();
            }

            @Override
            public void showContextMenu(View view, int position) {

            }
        });

        if(getIntent()!=null) {
            Bundle bundle = getIntent().getExtras();
            if(bundle!=null){
                if(bundle.containsKey("audioName")){
                    String audioName = bundle.getString("audioName");
                    et_input.setText(audioName);
                    getData(audioName);
                }
            }
        }
    }

    private void getData(String strKey) {
        RetrofitInterface retrofitInterface = HttpUtils.getInstance().getRetrofit(RetrofitInterface.class, "http://music.163.com");
        retrofitInterface.searchMusicInfo(strKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<MusicResponse>() {
                    @Override
                    public void onNext(@NonNull MusicResponse response) {
                        String str = JSON.toJSONString(response);
                        QDLogger.i("onNext: " + str);
                        if(response!=null&&response.getCode()==200){
                            netImageList.clear();
                            netImageList.addAll(response.getResult().getSongs());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    protected void onStart() {
                    }

                    @Override
                    public void onError(@NonNull Throwable throwable) {
                        QDLogger.i("onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                    }
                });

    }
}