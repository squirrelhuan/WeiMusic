package com.demomaster.weimusic.ui.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.MusicCollection;

import java.util.List;

public class MusicCollectAdapter extends BaseAdapter {
	// 帧动画
	private AnimationDrawable mPeakOneAnimation, mPeakTwoAnimation,
			mPeakThreeAnimation;
	private List<MusicCollection> musicList;
	private Context mContext;

	public MusicCollectAdapter(List<MusicCollection> musicList, Context mContext){
		this.mContext = mContext;
		this.musicList = musicList;
	}
	
	@Override
	public int getCount() {
		return musicList.size();
	}

	@Override
	public Object getItem(int position) {
		return musicList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return musicList.get(position).getId();
	}
	@SuppressWarnings("ResourceType")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_song_sheet_01, null);
			ImageView pImageView = (ImageView) convertView
					.findViewById(R.id.iv_head);
			TextView pTitle = (TextView) convertView
					.findViewById(R.id.tv_name);
			TextView pArtist = (TextView) convertView
					.findViewById(R.id.tv_count);

			viewHolder = new ViewHolder(pImageView, pTitle,pArtist);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.title.setText(musicList.get(position).getName());

		return convertView;
	}
	
	class ViewHolder {
		public ViewHolder(ImageView imageView, TextView title, TextView artist) {
			this.imageView = imageView;
			this.title = title;
			this.artist = artist;
		}

		ImageView imageView;
		TextView title;
		TextView artist;
	}

}
