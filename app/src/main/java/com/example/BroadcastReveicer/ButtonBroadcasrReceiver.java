package com.example.BroadcastReveicer;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.Const.Const;
import com.example.Interface.IMusic;
import com.example.Service.AudioService;

/**
 * Created by Super on 2015/5/29.
 */
public class ButtonBroadcasrReceiver extends BroadcastReceiver {
    private IMusic music;
    private NotificationManager mNotificationManager;
    private AudioService audioService;

    public ButtonBroadcasrReceiver() {
        super();
    }

    public ButtonBroadcasrReceiver(IMusic music,NotificationManager notify,AudioService audioService){
        this();
        this.music=music;
        this.mNotificationManager=notify;
        this.audioService=audioService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String ctrl_code = intent.getAction();//获取action标记，用户区分点击事件

        if (music != null) {
            if ("play".equals(ctrl_code)) {
                music.moveOn();
            } else if ("pause".equals(ctrl_code)) {
                music.pause();
            } else if ("next".equals(ctrl_code)) {
                music.nextSong();
            } else if ("last".equals(ctrl_code)) {
                music.prevSong();
            }
        }

        if ("cancel".equals(ctrl_code)) {
            if(music!=null){
                music.stop();
                music=null;
            }
            audioService.stopSelf();
            mNotificationManager.cancel(Const.NOTI_CTRL_ID);
            System.exit(0);
        }

    }
}
