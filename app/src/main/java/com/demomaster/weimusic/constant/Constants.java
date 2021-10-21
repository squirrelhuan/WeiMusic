package com.demomaster.weimusic.constant;

import android.os.Environment;

public class Constants {

	public final static String BASE_FILE_PATH = Environment.getExternalStorageDirectory() + "/PPMusic";

	public final static String APP_PATH_DOWNLOAD = BASE_FILE_PATH + "/download";

	public final static String APP_PATH_DOWNLOAD_HTML = BASE_FILE_PATH + "/html";

	public final static String APP_PATH_CACHE_DIR = BASE_FILE_PATH + "/cache";

	public final static String APP_PATH_PICTURE = BASE_FILE_PATH + "/picture";

    public final static String APP_PATH_PICTURE_CAVER = BASE_FILE_PATH + "/picture/caver";
    public final static String APP_PATH_PICTURE_WELCOME = BASE_FILE_PATH + "/picture/welcome";
    public final static String APP_PATH_PICTURE_WALLPAGER = BASE_FILE_PATH + "/picture/wallpager";
    public final static String APP_PATH_SHEET = BASE_FILE_PATH + "/sheet";

	public final static String APP_PATH_VIDEO = BASE_FILE_PATH + "/video";

	public static String Only_Use_Wifi = "OnlyUseWifi";//只使用wifi网络
	
	public static String Use_Net_To_Play = "UseNetToPlay";//使用数据网络播放歌曲
	
	public static String Use_Net_To_DownLoad = "UseNetToDownLoad";//使用数据网络下载歌曲
	
	public static String WallPager_Path = "WallPagerPath";//壁纸路径


    public static final String Action_Theme_Change = "Action_Theme_Change";
    /********************************************************************************/
    //广播
    public static final String Action_Theme_Font = "Action_Theme_Font";
    //区直
    public static final String Key_Theme_Font_Custom = "Key_Theme_Font_Custom";
    //区直
    public static final String Key_Theme_Font_System = "Key_Theme_Font_System";
    //自定义或者系统自带
    public static final String Theme_Font_Type = "Theme_Font_Type";
    //系统自带
    public static final int Theme_Font_Type_System = 0;
    //自定义
    public static final int Theme_Font_Type_Custom = 1;

/********************************************************************************/
    //广播
	public static final String Action_Theme_WallPager = "Action_Theme_WallPager";
	//区直
    public static final String Key_Theme_WallPager_Custom = "Key_Theme_WallPager_Custom";
    //区直
    public static final String Key_Theme_WallPager_System = "Key_Theme_WallPager_System";
    //自定义或者系统自带
    public static final String Theme_WallPager_Type = "Theme_WallPager_Type";

    //系统自带
    //public static final String Theme_WallPager_With_Music = "Theme_WallPager_With_Music";


    /********************************************************************************/
    //广播
    public static final String Action_Theme_Welcome = "Action_Theme_Welcome";
    //区直
    public static final String Key_Theme_Welcome_Custom = "Key_Theme_Welcome_Custom";
    //区直
    public static final String Key_Theme_Welcome_System = "Key_Theme_Welcome_System";
    //自定义或者系统自带
    public static final String Theme_Welcome_Type = "Theme_Welcome_Type";
    //系统自带
    public static final int Theme_Welcome_Type_System = 0;
    //自定义
    public static final int Theme_Welcome_Type_Custom = 1;/*
    //自定义
    public static final int Theme_Welcome_Type_ByMusic = 2;*/

    /********************************************************************************/
    //广播
    public static final String Action_Theme_Cover = "Action_Theme_Cover";
    //区直
    public static final String Key_Theme_Cover_Custom = "Key_Theme_Cover_Custom";
    //区直
    public static final String Key_Theme_Cover_System = "Key_Theme_Cover_System";
    //自定义或者系统自带
    public static final String Theme_Cover_Type = "Theme_Cover_Type";
    //系统自带
    public static final int Theme_Cover_Type_System = 0;
    //自定义
    public static final int Theme_Cover_Type_Custom = 1;
    //自定义
   // public static final String Theme_Cover_Type_ByMusic = "Theme_Cover_Type_ByMusic";

    // SharedPreferences
    public final static String APOLLO = "PaoPao", APOLLO_PREFERENCES = "apollopreferences",
            ARTIST_KEY = "artist", ALBUM_KEY = "album", ALBUM_ID_KEY = "albumid", NUMALBUMS = "num_albums",
            GENRE_KEY = "genres", ARTIST_ID = "artistid", NUMWEEKS = "numweeks",
            PLAYLIST_NAME_FAVORITES = "Favorites", PLAYLIST_NAME = "playlist", WIDGET_STYLE="widget_type",
            THEME_PACKAGE_NAME = "themePackageName", THEME_DESCRIPTION = "themeDescription",
            THEME_PREVIEW = "themepreview", THEME_TITLE = "themeTitle", VISUALIZATION_TYPE="visualization_type", 
            UP_STARTS_ALBUM_ACTIVITY = "upStartsAlbumActivity", TABS_ENABLED = "tabs_enabled";
    
    // Theme item type
    public final static int THEME_ITEM_BACKGROUND = 0, THEME_ITEM_FOREGROUND = 1;
    
  //Image Loading Constants
    public final static String TYPE_ARTIST = "artist", TYPE_ALBUM = "album", TYPE_GENRE = "genre",
    		TYPE_PLAYLIST  = "playlist", ALBUM_SUFFIX = "albartimg", ARTIST_SUFFIX = "artstimg", 
    		PLAYLIST_SUFFIX = "plylstimg", GENRE_SUFFIX = "gnreimg", SRC_FIRST_AVAILABLE = "first_avail",
    		SRC_LASTFM = "last_fm", SRC_FILE = "from_file", SRC_GALLERY = "from_gallery",
    		SIZE_NORMAL = "normal", SIZE_THUMB = "thumb";
    
 // Playlists
    public final static long PLAYLIST_UNKNOWN = -1, PLAYLIST_ALL_SONGS = -2, PLAYLIST_QUEUE = -3,
            PLAYLIST_NEW = -4, PLAYLIST_FAVORITES = -5, PLAYLIST_RECENTLY_ADDED = -6;
    
    // Genres
    public final static String[] GENRES_DB = {
            "Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop",
            "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock",
            "Techno", "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack",
            "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance",
            "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise",
            "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop",
            "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic",
            "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta",
            "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret",
            "New Wave", "Psychedelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal",
            "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock",
            "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin",
            "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock",
            "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band", "Chorus",
            "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber Music",
            "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam",
            "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul",
            "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", "Dance Hall",
            "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie", "Britpop",
            "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta Rap", "Heavy Metal",
            "Black Metal", "Crossover", "Contemporary Christian", "Christian Rock ", "Merengue",
            "Salsa", "Thrash Metal", "Anime", "JPop", "Synthpop"
    };
    
    // Storage Volume
    public final static String EXTERNAL = "external";
    
    // Bundle & Intent type
    public final static String MIME_TYPE = "mimetype", INTENT_ACTION = "action", DATA_SCHEME = "file";
    
    public final static String INTENT_ADD_TO_PLAYLIST = "com.demomaster.weimusic.ADD_TO_PLAYLIST",
            INTENT_PLAYLIST_LIST = "playlistlist",
            INTENT_CREATE_PLAYLIST = "com.demomaster.weimusic.CREATE_PLAYLIST",
            INTENT_RENAME_PLAYLIST = "com.demomaster.weimusic.RENAME_PLAYLIST",
            INTENT_KEY_RENAME = "rename", INTENT_KEY_DEFAULT_NAME = "default_name";
    
    // Last.fm API
    public final static String LASTFM_API_KEY = "0bec3f7ec1f914d7c960c12a916c8fb3";

    public static String Action_WallBackGround ="com.paopaomusic.ChangeWallBG";
    public static String Action_WellComeBackGround ="com.paopaomusic.ChangeWellComeBG";
}
