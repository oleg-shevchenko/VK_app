package com.olegsh.vkapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.olegsh.vkapp.R;
import com.olegsh.vkapp.utils.Utils;
import com.vk.sdk.api.model.VKApiVideo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleg on 07.11.2017.
 */

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoHolder> {

    private final String TAG = "VideosAdapter";

    private List<VKApiVideo> videoList;
    private LayoutInflater inflater;
    private OnVideoClickListener listener;
    private Context context;
    private ImageLoader imageLoader = ImageLoader.getInstance();

    public VideosAdapter(List<VKApiVideo> videoList, Context context) {
        if(videoList == null || videoList.size() == 0) {
            this.videoList = new ArrayList<>(0);
        } else {
            this.videoList = new ArrayList<>(videoList);
        }
        Log.d(TAG, "Adapter list size initial: " + this.videoList.size());
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listener = (OnVideoClickListener) context;
        this.context = context;
    }

    public void updateData(List<VKApiVideo> videoList, boolean clean) {
        if(clean) {
            this.videoList = new ArrayList<>(videoList);
        } else {
            this.videoList.addAll(videoList);
        }
        Log.d(TAG, "Adapter list size after update: " + this.videoList.size());
        notifyDataSetChanged();
    }

    @Override
    public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_video, parent, false);
        return new VideoHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoHolder holder, int position) {
        VKApiVideo video = videoList.get(position);
        holder.bindVideo(video);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    class VideoHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDuration;
        private ImageView ivPreview;

        VideoHolder(final View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.textTitle);
            tvDuration = (TextView) itemView.findViewById(R.id.textDuration);
            ivPreview = (ImageView) itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onVideoClick(videoList.get(getAdapterPosition()));
                }
            });
        }

        //Argument - int time in seconds, returns string in format HH:MM:SS
        String convertSeconds(int totalSecs) {
            int hours = totalSecs / 3600;
            int minutes = (totalSecs % 3600) / 60;
            int seconds = totalSecs % 60;
            return context.getString(R.string.tv_duration_2, hours, minutes, seconds);
        }

        //This method bind views to object fields
        void bindVideo(VKApiVideo video) {
            tvTitle.setText(video.title);
            tvDuration.setText(convertSeconds(video.duration));
            imageLoader.displayImage(video.photo_320, ivPreview, Utils.options);
        }
    }

    //Click on item callback interface
    public interface OnVideoClickListener {
        void onVideoClick(VKApiVideo video);
    }
}
