package com.olegsh.vkapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.olegsh.vkapp.R;
import com.olegsh.vkapp.adapter.VideosAdapter;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiVideo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleg on 07.11.2017.
 */

public class VideosActivity extends AppCompatActivity implements VideosAdapter.OnVideoClickListener {

    private final String TAG = "VideosActivity";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private VideosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);
        initView();
    }

    private void initView() {
        recyclerView = findViewById(R.id.itemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideosAdapter(null, this);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1)) {
                    swipeRefreshLayout.setRefreshing(true);
                    updateVideoList(false);
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    ImageLoader.getInstance().pause();
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    ImageLoader.getInstance().resume();
                }
            }
        });
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateVideoList(true);
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        updateVideoList(true);
    }

    private String next_from;
    //Update video list using "newsfeed.get" request
    private void updateVideoList(final boolean clean) {
        if(clean) next_from = null;
        VKParameters parameters = VKParameters.from(VKApiConst.FILTERS, "video", VKApiConst.COUNT, "10", "start_from", next_from);
        final VKRequest request = new VKRequest("newsfeed.get", parameters);
        request.setRequestListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d(TAG, response.json.toString());
                try {
                    List<VKApiVideo> newList = parseVideoListResponse(response.json);
                    onVideoListLoadComplete(newList, clean);
                } catch (JSONException e) {
                    e.printStackTrace();
                    VideosActivity.this.onError(e.getMessage());
                }
            }

            //Sorry for this. This is my super VK response JSON parser :)
            //TODO: I hope better way exist (not manual parsing)
            private List<VKApiVideo> parseVideoListResponse(JSONObject response) throws JSONException {
                next_from = response.getJSONObject("response").getString("next_from");
                List<VKApiVideo> listVideo = new ArrayList<>();
                JSONArray array = response.getJSONObject("response").getJSONArray("items");
                for(int i = 0; i < array.length(); i++) {
                    JSONArray jsonArray = array.getJSONObject(i).getJSONObject("video").getJSONArray("items");
                    for (int j = 0; j < jsonArray.length(); j++ ) {
                        JSONObject jsonObject = jsonArray.getJSONObject(j);
                        VKApiVideo video = new VKApiVideo();
                        video.parse(jsonObject);
                        listVideo.add(video);
                    }
                }
                return listVideo;
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                VideosActivity.this.onError(error.errorMessage);
            }
        });
        request.start();
    }

    //If video list is updated, than refresh adapter
    private void onVideoListLoadComplete(List<VKApiVideo> list, boolean clean) {
        adapter.updateData(list, clean);
        //recyclerView.swapAdapter(adapter, false);
        swipeRefreshLayout.setRefreshing(false);
    }

    //This method receives clicks on video items, than make new request with VK SDK to get "player" field
    //for selected video. Finally open activity with WebView to play video.
    @Override
    public void onVideoClick(VKApiVideo video) {
        VKParameters parameters = VKParameters.from("videos", video.owner_id + "_" + video.id);
        final VKRequest request = VKApi.video().get(parameters);
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d(TAG, response.json.toString());
                try {
                    String player = response.json.getJSONObject("response").getJSONArray("items").getJSONObject(0).getString("player");
                    Intent intent = new Intent(VideosActivity.this, VideoViewActivity.class);
                    intent.setAction(player);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                    VideosActivity.this.onError(e.getMessage());
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                VideosActivity.this.onError(error.errorMessage);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, getString(R.string.text_logout));
        menu.add(0, 2, 0, getString(R.string.txt_menu_about));
        menu.add(0, 3, 0, getString(R.string.txt_menu_exit));
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Logout menu item pressed. Logout using SDK, clear cache, start LoginActivity
            case 1: {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.text_logout))
                        .setMessage(getString(R.string.text_logout_question))
                        .setPositiveButton(getString(R.string.txt_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                VKSdk.logout();
                                ImageLoader.getInstance().clearDiskCache();
                                Intent intent = new Intent(VideosActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNeutralButton(getString(R.string.txt_cancel), null)
                        .create().show();
                break;
            }
            //About menu item pressed
            case 2: {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.txt_menu_about))
                        .setMessage(getString(R.string.txt_dialog_about))
                        .setPositiveButton(getString(R.string.txt_ok), null)
                        .create().show();
                break;
            }
            //Exit menu item pressed
            case 3: {
                finishAffinity();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //Error messages
    private void onError(String msg) {
        Snackbar.make(swipeRefreshLayout, msg, Snackbar.LENGTH_LONG).show();
    }

    //This block is used to implement double click back button to exit app
    private static long back_pressed;
    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, getString(R.string.back_to_exit_msg), Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();
    }
}
