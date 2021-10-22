package com.demomaster.weimusic.model;

import androidx.annotation.NonNull;

import cn.demomaster.quickdatabaselibrary.annotation.Constraints;
import cn.demomaster.quickdatabaselibrary.annotation.DBTable;
import cn.demomaster.quickdatabaselibrary.annotation.SQLObj;


@DBTable(name = "AudioInfo")
public class AudioInfo /*implements Parcelable*/{
	@SQLObj(name = "id",constraints = @Constraints(autoincrement = true,primaryKey = true))
	public long id;
	@SQLObj(name = "audioId")
	public long audioId;
	@SQLObj(name = "title")
	public String title;
	public String album;
	@SQLObj(name = "duration")
	public int duration;
	public int position;
	public long size;
	public long albumId;
	@SQLObj(name = "is_favorite")
	public boolean favourite;
	public String artist;
	public String url;
	//public String path;
	public String data;
	@SQLObj(name = "sheetId")
	public long sheetId=-1;
	private int resourceType=0;//资源类型 0 file 1url网络资源
	@SQLObj(name = "md5")
	private String md5;
	
	public AudioInfo(){
		
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public AudioInfo(long pId){
		id = pId;
	}
	
	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}		

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}	

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isFavourite() {
		return favourite;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}

	/*@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(title);
		dest.writeString(album);
		dest.writeString(artist);
		dest.writeString(url);
		dest.writeInt(duration);
		dest.writeLong(size);
	}*/
	
/*	public static final Parcelable.Creator<MusicInfo> 
		CREATOR = new Creator<MusicLoader.MusicInfo>() {
		
		@Override
		public MusicInfo[] newArray(int size) {
			return new MusicInfo[size];
		}
		
		@Override
		public MusicInfo createFromParcel(Parcel source) {
			MusicInfo musicInfo = new MusicInfo();
			musicInfo.setId(source.readLong());
			musicInfo.setTitle(source.readString());
			musicInfo.setAlbum(source.readString());
			musicInfo.setArtist(source.readString());
			musicInfo.setUrl(source.readString());
			musicInfo.setDuration(source.readInt());
			musicInfo.setSize(source.readLong());
			return musicInfo;
		}
	};*/

	public long getSheetId() {
		return sheetId;
	}

	public void setSheetId(long sheetId) {
		this.sheetId = sheetId;
	}

	/*public String getPath() {
		return "";
	}*/

	/*public void setPath(String path) {
		this.path = path;
	}*/

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@NonNull
	@Override
	public String toString() {
		return id+","+title+","+artist+","+album+",duration="+duration+",position="+position+",path"+getData();
	}

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public long getAudioId() {
		return audioId;
	}

	public void setAudioId(long audioId) {
		this.audioId = audioId;
	}

	public int getResourceType() {
		return resourceType;
	}

	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}
}
