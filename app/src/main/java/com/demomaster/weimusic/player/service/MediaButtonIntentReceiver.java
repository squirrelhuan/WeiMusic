/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.demomaster.weimusic.player.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;

import com.demomaster.weimusic.activity.MainActivity;

import cn.demomaster.huan.quickdeveloplibrary.helper.toast.QdToast;
import cn.demomaster.qdlogger_library.QDLogger;

/**
 * 通知栏操作
 */
public class MediaButtonIntentReceiver extends BroadcastReceiver {

    private static final int MSG_LONGPRESS_TIMEOUT = 1;
    private static boolean mLaunched = false;

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LONGPRESS_TIMEOUT:
                    if (!mLaunched) {
                        Context context = (Context) msg.obj;
                        Intent i = new Intent();
                        i.putExtra("autoshuffle", "true");
                        //TODO- i.setClass(context, AudioPlayerHolder.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);
                        mLaunched = true;
                    }
                    break;
            }
        }
    };

    private static long firstTime = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        QDLogger.println("intentAction=" + intentAction);
        /*
            Intent i = new Intent(context, MusicService.class);
            // i.setAction(ApolloService.SERVICECMD);
            // i.putExtra(ApolloService.CMDNAME, ApolloService.CMDPAUSE);
            // context.startService(i);
        }*/
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
            //来电/耳机拔出时暂停播放3
            MC.getInstance(context).pause();
        }else if (intentAction.equals(Intent.ACTION_HEADSET_PLUG)) {
            if (intent.getIntExtra("state", 0) == 1) {
                QDLogger.println("耳机检测：插入");
            } else {
                QDLogger.println("耳机检测：拔出");
            }
        } else if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                return;
            }
            int keycode = event.getKeyCode();
            int action = event.getAction();
            long eventtime = event.getEventTime();
            //int buttonId = intent.getIntExtra(ApolloService.CMDNOTIF, 0);

            // single quick press: pause/resume.
            // double press: next track
            // long press: start auto-shuffle mode.
            if(event.getRepeatCount() == 0&& action == KeyEvent.ACTION_DOWN) {
                //短按
                String command = null;
                switch (keycode) {
                    case KeyEvent.KEYCODE_MEDIA_STOP:
                        //command = ApolloService.CMDSTOP;
                        break;
                    case KeyEvent.KEYCODE_HEADSETHOOK:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        long secondTime = System.currentTimeMillis();
                        if (secondTime - firstTime > 350) {
                            //QDLogger.e("暂停/播放"+(secondTime - firstTime));
                            firstTime = secondTime;
                            if (MC.getInstance(context).isPlaying()) {
                                MC.getInstance(context).pause();
                            } else {
                                MC.getInstance(context).play();
                            }
                        } else {
                            //双击播放下一首
                            MC.getInstance(context).playNext();
                        }
                        //command = ApolloService.CMDTOGGLEPAUSE;
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        MC.getInstance(context).playNext();
                        // command = ApolloService.CMDNEXT;
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        MC.getInstance(context).playPrev();
                        //  command = ApolloService.CMDPREVIOUS;
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        MC.getInstance(context).pause();
                        // command = ApolloService.CMDPAUSE;
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        MC.getInstance(context).play();
                        // command = ApolloService.CMDPLAY;
                        break;
                }
            }
        }
            /*if (command != null) {
                if (action == KeyEvent.ACTION_DOWN) {
                    if (mDown && (buttonId == 0)) {
                        if ((ApolloService.CMDTOGGLEPAUSE.equals(command) || ApolloService.CMDPLAY
                                .equals(command))
                                && mLastClickTime != 0
                                && eventtime - mLastClickTime > LONG_PRESS_DELAY) {
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_LONGPRESS_TIMEOUT,
                                    context));
                        }
                    } else if (event.getRepeatCount() == 0) {
                        // only consider the first event in a sequence, not the
                        // repeat events,
                        // so that we don't trigger in cases where the first
                        // event went to
                        // a different app (e.g. when the user ends a phone call
                        // by
                        // long pressing the headset button)

                        // The service may or may not be running, but we need to
                        // send it
                        // a command.
                        Intent i = new Intent(context, ApolloService.class);
                        i.setAction(ApolloService.SERVICECMD);
                        i.putExtra(ApolloService.CMDNOTIF, buttonId);
                        if (keycode == KeyEvent.KEYCODE_HEADSETHOOK
                                && eventtime - mLastClickTime < 300) {
                            i.putExtra(ApolloService.CMDNAME, ApolloService.CMDNEXT);
                            context.startService(i);
                            mLastClickTime = 0;
                        } else {
                            i.putExtra(ApolloService.CMDNAME, command);
                            context.startService(i);
                            mLastClickTime = eventtime;
                        }

                        mLaunched = false;
                        if (buttonId == 0) {
                            mDown = true;
                        }
                    }
                } else {
                    mHandler.removeMessages(MSG_LONGPRESS_TIMEOUT);
                    mDown = false;
                }
                if (isOrderedBroadcast()) {
                    abortBroadcast();
                }
            }
        }*/
    }
}
