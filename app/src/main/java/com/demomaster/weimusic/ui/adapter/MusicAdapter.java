package com.demomaster.weimusic.ui.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;


import com.demomaster.weimusic.R;
import com.demomaster.weimusic.interfaces.FilterListener;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.player.service.MC;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends BaseAdapter implements Filterable {
    // 帧动画
    private AnimationDrawable mPeakOneAnimation, mPeakTwoAnimation,
            mPeakThreeAnimation;
    private List<AudioInfo> musicList;
    private Context mContext;
    private MyFilter filter = null;// 创建MyFilter对象
    private FilterListener listener = null;// 接口对象

    public MusicAdapter(List<AudioInfo> musicList, Context mContext, FilterListener filterListener) {
        this.mContext = mContext;
        this.musicList = musicList;
        this.listener = filterListener;
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

    /**
     * Used to quickly our the ContextMenu
     */
    private final View.OnClickListener showContextMenu = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.showContextMenu();
        }
    };

    @SuppressWarnings("ResourceType")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.music_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tv_index.setText((position +1)+ "");
        //viewHolder.imageView.setImageResource(R.drawable.audio);
        viewHolder.title.setText(musicList.get(position).getTitle());
        viewHolder.iv_more.setOnClickListener(showContextMenu);
        viewHolder.artist.setText(musicList.get(position).getArtist());

        long currentaudioid = MC.getInstance(mContext).getCurrentAudioId();
        long audioid = musicList.get(position).getId();
        if (currentaudioid == audioid) {
            viewHolder.mPeakOne
                    .setImageResource(R.anim.peak_white_1);
            viewHolder.mPeakTwo
                    .setImageResource(R.anim.peak_white_2);
            viewHolder.mPeakThree
                    .setImageResource(R.anim.peak_white_3);
            mPeakOneAnimation = (AnimationDrawable) viewHolder.mPeakOne
                    .getDrawable();
            mPeakTwoAnimation = (AnimationDrawable) viewHolder.mPeakTwo
                    .getDrawable();
            mPeakThreeAnimation = (AnimationDrawable) viewHolder.mPeakThree
                    .getDrawable();
            if (MC.getInstance(mContext).isPlaying()) {
                mPeakOneAnimation.start();
                mPeakTwoAnimation.start();
                mPeakThreeAnimation.start();
            } else {
                mPeakOneAnimation.stop();
                mPeakTwoAnimation.stop();
                mPeakThreeAnimation.stop();
            }
        } else {
            viewHolder.mPeakOne.setImageResource(0);
            viewHolder.mPeakTwo.setImageResource(0);
            viewHolder.mPeakThree.setImageResource(0);
        }

        return convertView;
    }

    class ViewHolder {
        public ViewHolder(View convertView) {
            title = convertView
                    .findViewById(R.id.title);
            iv_more = convertView
                    .findViewById(R.id.iv_more);
            artist = (TextView) convertView
                    .findViewById(R.id.artist);
            mPeakOne = convertView.findViewById(R.id.peak_one);
            mPeakTwo = convertView.findViewById(R.id.peak_two);
            mPeakThree = convertView.findViewById(R.id.peak_three);
            tv_index = convertView.findViewById(R.id.tv_index);
        }

        ImageView mPeakOne, mPeakTwo, mPeakThree, iv_more;
        TextView title, tv_index;
        TextView artist;
    }


    @Override
    public Filter getFilter() {
        // 如果MyFilter对象为空，那么重写创建一个
        if (filter == null) {
            filter = new MyFilter(musicList);
        }
        return filter;
    }


    /**
     * 创建内部类MyFilter继承Filter类，并重写相关方法，实现数据的过滤
     *
     * @author 邹奇
     */
    class MyFilter extends Filter {
        // 创建集合保存原始数据
        private List<AudioInfo> original = new ArrayList<AudioInfo>();

        public MyFilter(List<AudioInfo> list) {
            this.original = list;
        }

        /**
         * 该方法返回搜索过滤后的数据
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // 创建FilterResults对象
            FilterResults results = new FilterResults();

            /**
             * 没有搜索内容的话就还是给results赋值原始数据的值和大小
             * 执行了搜索的话，根据搜索的规则过滤即可，最后把过滤后的数据的值和大小赋值给results
             *
             */
            if (TextUtils.isEmpty(constraint)) {
                results.values = original;
                results.count = original.size();
            } else {
                // 创建集合保存过滤后的数据
                List<AudioInfo> mList = new ArrayList<AudioInfo>();
                // 遍历原始数据集合，根据搜索的规则过滤数据
                for (AudioInfo s : original) {
                    // 这里就是过滤规则的具体实现【规则有很多，大家可以自己决定怎么实现】
                    if (s.getTitle().trim().toLowerCase().contains(constraint.toString().trim().toLowerCase())) {
                        // 规则匹配的话就往集合中添加该数据
                        mList.add(s);
                    }
                }
                results.values = mList;
                results.count = mList.size();
            }

            // 返回FilterResults对象
            return results;
        }

        /**
         * 该方法用来刷新用户界面，根据过滤后的数据重新展示列表
         */
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            // 获取过滤后的数据
            musicList = (List<AudioInfo>) results.values;
            // 如果接口对象不为空，那么调用接口中的方法获取过滤后的数据，具体的实现在new这个接口的时候重写的方法里执行
            if (listener != null) {
                listener.getFilterData(musicList);
            }
            // 刷新数据源显示
            notifyDataSetChanged();
        }
    }

}
