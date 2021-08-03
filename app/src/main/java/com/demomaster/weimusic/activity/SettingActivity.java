package com.demomaster.weimusic.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.view.LinearLayout_withRaidoButton;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.helper.QDSharedPreferences;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.button.ToggleButton;
import cn.demomaster.huan.quickdeveloplibrary.widget.popup.QDPopup;
import cn.demomaster.quicksticker_annotations.BindView;
import cn.demomaster.quicksticker_annotations.QuickStickerBinder;


/**
 * 泡泡音乐设置界面
 *
 * @author SquirrelNuts
 */
public class SettingActivity extends BaseActivity implements OnClickListener {

    LinearLayout ll_only_wifi;// wifi模式
    LinearLayout ll_net_play;// 是否数据流量播放
    LinearLayout ll_net_download;// 数据流量下载
    LinearLayout ll_music_quality_play;// 播放品质
    LinearLayout ll_music_quality_download;// 下载品质
    LinearLayout ll_download_path;// 存储路径
    //LinearLayout ll_custom_wellcomepager;//自定义欢迎页
    //LinearLayout ll_custom_wallpager;// 自定义壁纸
    @BindView(R.id.tv_theme)
    TextView tv_theme;
    LinearLayout ll_automatic_play;// 自动播放
    LinearLayout ll_automatic_pause;
    @BindView(R.id.tv_help_and_suggest)
    TextView tv_help_and_suggest;// 帮助与建议
    @BindView(R.id.tv_about_player)
    TextView tv_about_player;// 关于泡泡音乐

    ListView lv_songs;
    @BindView(R.id.button_only_wifi)
    ToggleButton button_only_wifi;
    @BindView(R.id.button_net_play)
    ToggleButton button_net_play;
    @BindView(R.id.button_net_download)
    ToggleButton button_net_download;
    TextView tv_only_wifi, tv_net_play, tv_net_download, tv_wallpage_path,
            tv_welcomepage_path, tv_download_storage;
    String search_str = "周杰伦";

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_music_setting);
        QuickStickerBinder.getInstance().bind(this);
        setTitle("设置");
        initView();
    }

    void initView() {
        tv_only_wifi = (TextView) findViewById(R.id.tv_only_wifi);
        tv_net_play = (TextView) findViewById(R.id.tv_net_play);
        tv_net_download = (TextView) findViewById(R.id.tv_net_download);

        ll_only_wifi = (LinearLayout) findViewById(R.id.ll_only_wifi);
        ll_only_wifi.setOnClickListener(this);
        button_only_wifi = findViewById(R.id.button_only_wifi);
        button_only_wifi.setChecked(QDSharedPreferences.getInstance().getBoolean(
                "IsNetWifi",true));

        button_only_wifi.setOnToggleChanged(new ToggleButton.OnToggleChangeListener() {
            @Override
            public void onToggle(View view, boolean on) {
                    QDSharedPreferences.getInstance().putBoolean("IsNetWifi", on);
                    if (on) {
                        TypedValue typedValue = new TypedValue();
                        getTheme().resolveAttribute(R.attr.textNight, typedValue, true);
                        tv_net_play.setTextColor(typedValue.data);
                        tv_net_download.setTextColor(typedValue.data);
                        ll_net_play.setClickable(false);
                        ll_net_download.setClickable(false);
                    } else {
                        TypedValue typedValue = new TypedValue();
                        getTheme().resolveAttribute(R.attr.textNight2, typedValue, true);
                        tv_net_play.setTextColor(typedValue.data);
                        tv_net_download.setTextColor(typedValue.data);
                        ll_net_play.setClickable(true);
                        ll_net_download.setClickable(true);
                    }
                    Toast.makeText(mContext,
                            "only wifi " + (on ? "open" : "close"), Toast.LENGTH_SHORT).show();
            }
        });
        ll_net_play = (LinearLayout) findViewById(R.id.ll_net_play);
        ll_net_play.setOnClickListener(this);
        button_net_play = (ToggleButton) findViewById(R.id.button_net_play);
        button_net_play.setChecked(QDSharedPreferences.getInstance().getBoolean(
                "UseNetPlay",false));
        button_net_play.setOnToggleChanged(new ToggleButton.OnToggleChangeListener() {
            @Override
            public void onToggle(View view, boolean on) {
                QDSharedPreferences.getInstance().putBoolean("UseNetPlay", on);
                Toast.makeText(mContext,
                        "only wifi " + (on ? "open" : "close"), Toast.LENGTH_SHORT).show();
            }
        });
        ll_net_download = (LinearLayout) findViewById(R.id.ll_net_download);
        ll_net_download.setOnClickListener(this);
        button_net_download = (ToggleButton) findViewById(R.id.button_net_download);
        button_net_download.setChecked(QDSharedPreferences.getInstance().getBoolean(
                "UseNetDown",false));
        button_net_download.setOnToggleChanged(new ToggleButton.OnToggleChangeListener() {
            @Override
            public void onToggle(View view, boolean on) {
                QDSharedPreferences.getInstance().putBoolean("UseNetDown", on);
                Toast.makeText(mContext,
                        "only wifi " + (on ? "open" : "close"), Toast.LENGTH_SHORT).show();
            }
        });

        if (QDSharedPreferences.getInstance().getBoolean("IsNetWifi",false)) {
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.textNight, typedValue, true);
            tv_net_play.setTextColor(typedValue.data);
            tv_net_download.setTextColor(typedValue.data);
            ll_net_play.setClickable(false);
            ll_net_download.setClickable(false);
            button_net_play.setClickable(false);
            button_net_download.setClickable(false);
        } else {
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.textNight2, typedValue, true);
            tv_net_play.setTextColor(typedValue.data);
            tv_net_download.setTextColor(typedValue.data);
            ll_net_play.setClickable(true);
            ll_net_download.setClickable(true);
            button_net_play.setClickable(false);
            button_net_download.setClickable(false);
        }
        tv_theme.setOnClickListener(this);
        ll_music_quality_play = (LinearLayout) findViewById(R.id.ll_music_quality_play);
        ll_music_quality_play.setOnClickListener(this);
        ll_music_quality_download = (LinearLayout) findViewById(R.id.ll_music_quality_download);
        ll_music_quality_download.setOnClickListener(this);
        ll_download_path = (LinearLayout) findViewById(R.id.ll_download_path);
        ll_download_path.setOnClickListener(this);
        /*ll_custom_wallpager = (LinearLayout) findViewById(R.id.ll_custom_wallpager);
        ll_custom_wallpager.setOnClickListener(this);
		ll_custom_wellcomepager = (LinearLayout) findViewById(R.id.ll_custom_welcomepager);
		ll_custom_wellcomepager.setOnClickListener(this);*/
        ll_automatic_play = (LinearLayout) findViewById(R.id.ll_automatic_play);
        ll_automatic_play.setOnClickListener(this);
        ll_automatic_pause = (LinearLayout) findViewById(R.id.ll_automatic_pause);
        ll_automatic_pause.setOnClickListener(this);
        tv_help_and_suggest.setOnClickListener(this);
        tv_about_player.setOnClickListener(this);

        boolean network_permission = QDSharedPreferences.getInstance().getBoolean(
                "IsNetWifi",false);
        if (network_permission) {
            ll_net_play.setClickable(false);
            ll_net_download.setClickable(false);
        }

		/*tv_welcomepage_path = (TextView) findViewById(R.id.tv_welcomepage_path);
		tv_welcomepage_path.setText(MyApp.getPreferencesService().getValue(
				"WelcomePagePath", "默认"));
		tv_wallpage_path = (TextView) findViewById(R.id.tv_wallpage_path);
		tv_wallpage_path.setText(MyApp.getPreferencesService().getValue(
				"WallPagerPath", "默认"));*/
        tv_download_storage = (TextView) findViewById(R.id.tv_download_storage);
        tv_download_storage.setText(QDSharedPreferences.getInstance().getInt(
                "DownLoadStorage", 0) == 0 ? "手机存储" : "SD卡存储");

        initDriDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QuickStickerBinder.getInstance().unBind(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_only_wifi:
                button_only_wifi.performClick();
                break;
            case R.id.ll_net_play:
                button_net_play.performClick();
                break;
            case R.id.ll_net_download:
                button_net_download.performClick();
                break;
		/*case R.id.ll_custom_welcomepager://自定义欢迎页
			SelectPhotoPopupWindow("WelcomePagePath");
			break;
		case R.id.ll_custom_wallpager://自定义背景
			SelectPhotoPopupWindow("WallPagerPath");
			break;*/
            case R.id.tv_theme://自定义
                startActivity(ThemeActivity.class);
                break;
            case R.id.ll_download_path:
                mChooseMachineTypeDialog.show();
                break;
            case R.id.ll_music_quality_play:// 播放品质
                SelectQualityPopupWindow(0);
                break;
            case R.id.ll_music_quality_download:// 下载品质
                SelectQualityPopupWindow(1);
                break;
            case R.id.tv_about_player://自定义
                Intent intent1 = new Intent(mContext, AboutActivity.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }

    // 选择图片dialog
    private QDPopup pop = null;
    private LinearLayout ll_popup;
    private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CROP_SMALL_PICTURE = 5;
    private static final String LOG_TAG = "CGQ";
    private static String operationType = "WallPagerPath";

    /**
     * 选择品质的popupWindow
     *
     * @param type 0 播放品质 1下载品质
     */
    private void SelectQualityPopupWindow(int type) {
        pop = new QDPopup(this);
        View view = getLayoutInflater().inflate(
                R.layout.item_popupwindows_selected_quality_ios, null);

        ll_popup = view.findViewById(R.id.ll_popup);
        pop.setWidth(LayoutParams.MATCH_PARENT);
        pop.setHeight(LayoutParams.WRAP_CONTENT);
       // pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);

        RelativeLayout parent = view.findViewById(R.id.parent);
        parent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                ll_popup.startAnimation(AnimationUtils.loadAnimation(
                        mContext, R.anim.scaling_big));
            }
        });
        List<RadioButton> list = new ArrayList<RadioButton>();
        LinearLayout_withRaidoButton ll_btn_01 =  view
                .findViewById(R.id.item_popupwindows_ll_btn_01);
        RadioButton item_popupwindows_rb_001 = (RadioButton) view.findViewById(R.id.item_popupwindows_rb_001);
        item_popupwindows_rb_001.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
        ll_btn_01.setList(list);
        ll_btn_01.setRadioButton(item_popupwindows_rb_001);

        LinearLayout_withRaidoButton ll_btn_02 = (LinearLayout_withRaidoButton) view
                .findViewById(R.id.item_popupwindows_ll_btn_02);
        RadioButton item_popupwindows_rb_002 = (RadioButton) view.findViewById(R.id.item_popupwindows_rb_002);
        item_popupwindows_rb_002.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
        ll_btn_02.setList(list);
        ll_btn_02.setRadioButton(item_popupwindows_rb_002);

        LinearLayout_withRaidoButton ll_btn_03 = (LinearLayout_withRaidoButton) view
                .findViewById(R.id.item_popupwindows_ll_btn_03);
        RadioButton item_popupwindows_rb_003 = (RadioButton) view.findViewById(R.id.item_popupwindows_rb_003);
        item_popupwindows_rb_003.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
        ll_btn_03.setList(list);
        ll_btn_03.setRadioButton(item_popupwindows_rb_003);

        LinearLayout_withRaidoButton ll_btn_04 = (LinearLayout_withRaidoButton) view
                .findViewById(R.id.item_popupwindows_ll_btn_04);
        RadioButton item_popupwindows_rb_004 = (RadioButton) view.findViewById(R.id.item_popupwindows_rb_004);
        item_popupwindows_rb_004.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
        ll_btn_04.setList(list);
        ll_btn_04.setRadioButton(item_popupwindows_rb_004);

        LinearLayout_withRaidoButton ll_btn_05 = (LinearLayout_withRaidoButton) view
                .findViewById(R.id.item_popupwindows_ll_btn_05);
        RadioButton item_popupwindows_rb_005 = (RadioButton) view.findViewById(R.id.item_popupwindows_rb_005);
        item_popupwindows_rb_005.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
        ll_btn_05.setList(list);
        ll_btn_05.setRadioButton(item_popupwindows_rb_005);

        pop.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        ll_popup.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.bottom_up));
    }

    Dialog mChooseMachineTypeDialog;
    TextView tv_storage_phone, tv_storage_extra;
    RadioButton rb_phone, rb_extra;
    LinearLayout ll_no_sdcard, ll_has_sdcard;

    /**
     * 选择存储Dialog
     */
    private void initDriDialog() {
        View mChooseTypeDialogView = this.getLayoutInflater().inflate(
                R.layout.choose_download_path_item, null);
        tv_storage_phone = (TextView) mChooseTypeDialogView
                .findViewById(R.id.tv_storage_phone);
        tv_storage_phone.setText(QDFileUtil.formatFileSize(
                QDFileUtil.getAvailableInternalMemorySize(), true)
                + "可用，共"
                + QDFileUtil.formatFileSize(
                QDFileUtil.getTotalInternalMemorySize(), true));
        ll_has_sdcard = mChooseTypeDialogView
                .findViewById(R.id.ll_has_sdcard);
        ll_no_sdcard =  mChooseTypeDialogView
                .findViewById(R.id.ll_no_sdcard);
        if (!QDFileUtil.externalMemoryAvailable()) {
            ll_has_sdcard.setVisibility(View.GONE);
            ll_no_sdcard.setVisibility(View.VISIBLE);
        } else {
            tv_storage_extra =  mChooseTypeDialogView
                    .findViewById(R.id.tv_storage_extra);
            tv_storage_extra.setText(QDFileUtil.formatFileSize(
                    QDFileUtil.getAvailableExternalMemorySize(), true)
                    + "可用，共"
                    + QDFileUtil.formatFileSize(
                    QDFileUtil.getTotalExternalMemorySize(), true));
        }
        rb_phone = (RadioButton) mChooseTypeDialogView
                .findViewById(R.id.rb_phone);
        rb_phone.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (arg1) {
                    rb_extra.setChecked(false);
                    QDSharedPreferences.getInstance().putInt("DownLoadStorage", 0);
                    tv_download_storage.setText(QDSharedPreferences.getInstance()
                            .getInt("DownLoadStorage", 0) == 0 ? "手机存储"
                            : "SD卡存储");
                }
            }
        });
        rb_extra = (RadioButton) mChooseTypeDialogView
                .findViewById(R.id.rb_extra);
        rb_extra.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (arg1) {
                    rb_phone.setChecked(false);
                    QDSharedPreferences.getInstance().putInt("DownLoadStorage", 1);
                    tv_download_storage.setText(QDSharedPreferences.getInstance()
                            .getInt("DownLoadStorage", 0) == 0 ? "手机存储"
                            : "SD卡存储");
                }
            }
        });
        mChooseMachineTypeDialog = new Dialog(this);
        mChooseMachineTypeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mChooseMachineTypeDialog.setCanceledOnTouchOutside(true);
        mChooseMachineTypeDialog.setContentView(mChooseTypeDialogView);
    }

    public static void getImageFromPhoto(Context context, int REQUE_CODE_PHOTO) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        ((Activity) context).startActivityForResult(intent, REQUE_CODE_PHOTO);
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = null;
        try {
            // This location works best if you want the created images to be
            // shared
            // between applications and persist after your app has been
            // uninstalled.
            mediaStorageDir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "MyCameraApp");

            Log.d(LOG_TAG, "Successfully created mediaStorageDir: "
                    + mediaStorageDir);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Error in Creating mediaStorageDir: "
                    + mediaStorageDir);
        }
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(LOG_TAG,"failed to create directory, check if you have the WRITE_EXTERNAL_STORAGE permission");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }

    // 获取返回结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (pop != null) {
            pop.dismiss();
        }
    }

    protected void setImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            // photo = ImageUtils.toRoundBitmap(photo, fileUri); //
            // ���ʱ���ͼƬ�Ѿ��������Բ�ε���
            // iv_personal_icon.setImageBitmap(photo);
            // uploadPic(photo);
        }
    }
}
