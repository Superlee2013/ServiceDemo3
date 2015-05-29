package com.example.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.example.Const.Const;
import com.example.Interface.IMusic;
import com.example.servicedemo3.MainActivity;
import com.example.servicedemo3.R;

import java.io.IOException;

/**
 * Created by Super on 2015/5/29.
 */
public class AudioService extends Service {

    private MediaPlayer myMusicMediaPlayer;
    public ButtonBroadcastReceiver bReceiver;
    public NotificationManager mNotificationManager;
    private int songPos;
    private IMusic iMusic;

    private boolean isPause=false;

    public final static String ACTION_BUTTON = "com.example.Superlee.ButtonClick";


    public TextView txtView;

    RemoteViews contentView;
    Notification notification;

    public String songName;


    @Override
    public void onCreate() {
        super.onCreate();
        songPos=MainActivity.mCurrentSongIndex;
        playSong(songPos);
        iMusic = new Music();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        initBroadcastReceiver();
        mNotificationManager.cancel(Const.NOTI_CTRL_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*if (myMusicMediaPlayer.isPlaying()) {
            myMusicMediaPlayer.stop();
        }*/
        stopTheSong();

        mNotificationManager.cancel(Const.NOTI_CTRL_ID);
        unregisterReceiver(bReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!myMusicMediaPlayer.isPlaying()) {
            myMusicMediaPlayer.start();
        }
        initNotificationBar();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void initBroadcastReceiver() {
        bReceiver=new ButtonBroadcastReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ACTION_BUTTON);
        registerReceiver(bReceiver, intentFilter);
    }

    public void playSong(int songPos) {

        if (songPos >= 0) {
            songPos = songPos % Const.SONG_INFO.length;
        } else {
            songPos = Const.SONG_INFO.length + songPos;
        }

        if (myMusicMediaPlayer == null) {
            myMusicMediaPlayer = new MediaPlayer();
        }


        myMusicMediaPlayer.reset();

        AssetManager assetManager = getBaseContext().getAssets();

        songPos = songPos % Const.SONG_INFO.length;

        String songFileName = Const.SONG_INFO[songPos][Const.SONG_FILE_NAME];
        songName=Const.SONG_INFO[songPos][Const.SONG_NAME];

        MainActivity.mCurrentSongIndex=songPos;

        try {
            AssetFileDescriptor fileDescriptor = assetManager.openFd(songFileName);

            myMusicMediaPlayer.setDataSource(
                    fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength()
            );

            myMusicMediaPlayer.prepare();

            myMusicMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopTheSong() {
        if (myMusicMediaPlayer != null) {
            myMusicMediaPlayer.stop();
            myMusicMediaPlayer.release();
            myMusicMediaPlayer = null;
        }
    }

    public final static String INTENT_BUTTONID_TAG = "ButtonId";
    /**
     * 上一首 按钮点击 ID
     */
    public final static int BUTTON_PREV_ID = 1;
    /**
     * 播放/暂停 按钮点击 ID
     */
    public final static int BUTTON_PALY_ID = 2;
    /**
     * 下一首 按钮点击 ID
     */
    public final static int BUTTON_NEXT_ID = 3;

    public final static int BUTTON_PAUSE_ID = 4;
    public final static int BUTTON_CANCLE_ID = 5;


    public void initNotificationBar() {


        notification = new Notification();
        notification.icon = R.drawable.ic_launcher;

        contentView = new RemoteViews(getPackageName(), R.layout.notification_control);
        notification.contentView = contentView;

        Intent buttonIntent = new Intent(ACTION_BUTTON);
        /* 上一首按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PREV_ID);
        //这里加了广播，所及INTENT的必须用getBroadcast方法
        PendingIntent intent_prev = PendingIntent.getBroadcast(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.bt_notic_last, intent_prev);
        /* 播放/暂停  按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PALY_ID);
        PendingIntent intent_paly = PendingIntent.getBroadcast(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.bt_notic_play, intent_paly);
        /* 下一首 按钮  */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_NEXT_ID);
        PendingIntent intent_next = PendingIntent.getBroadcast(this, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.bt_notic_next, intent_next);

        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PAUSE_ID);
        PendingIntent intent_pause = PendingIntent.getBroadcast(this, 4, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.bt_notic_pause, intent_pause);

        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_CANCLE_ID);
        PendingIntent intent_cancle = PendingIntent.getBroadcast(this, 5, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.bt_notic_cancel, intent_cancle);

        notification.contentView.setTextViewText(R.id.txt_title,songName);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(Const.NOTI_CTRL_ID, notification);
    }

    public class ButtonBroadcastReceiver extends BroadcastReceiver {

        public ButtonBroadcastReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();//获取action标记，用户区分点击事件

            if (action.equals(ACTION_BUTTON)) {
                int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
                if (iMusic != null) {
                    switch (buttonId) {
                        case BUTTON_PREV_ID:
                            iMusic.prevSong();
                            break;
                        case BUTTON_PALY_ID:
                            iMusic.moveOn();
                            break;
                        case BUTTON_NEXT_ID:
                            iMusic.nextSong();
                            break;
                        case BUTTON_PAUSE_ID:
                            iMusic.pause();
                            break;
                        /*case BUTTON_CANCLE_ID:
                            iMusic.stop();
                            mNotificationManager.cancel(Const.NOTI_CTRL_ID);
                            AudioService.this.stopSelf();
                            break;*/
                        default:
                            break;
                    }
                    notification.contentView.setTextViewText(R.id.txt_title,songName);
                    mNotificationManager.notify(Const.NOTI_CTRL_ID, notification);

                }
                if(buttonId==BUTTON_CANCLE_ID){
                    if(iMusic!=null){
                        iMusic.stop();
                    }
                    if(mNotificationManager!=null){
                        mNotificationManager.cancel(Const.NOTI_CTRL_ID);
                    }
                    AudioService.this.stopSelf();
                }
            }

            /*if (iMusic != null) {
                if ("play".equals(ctrl_code)) {
                    iMusic.moveOn();
                } else if ("pause".equals(ctrl_code)) {
                    iMusic.pause();
                } else if ("next".equals(ctrl_code)) {
                    iMusic.nextSong();
                } else if ("last".equals(ctrl_code)) {
                    iMusic.prevSong();
                }
            }

            if ("cancel".equals(ctrl_code)) {
                if(iMusic!=null){
                    iMusic.stop();
                    iMusic=null;
                }
                AudioService.this.stopSelf();
                mNotificationManager.cancel(Const.NOTI_CTRL_ID);
                System.exit(0);
            }*/

        }
    }


    private class Music implements IMusic {

        @Override
        public void moveOn() {
            myMusicMediaPlayer.start();
        }

        @Override
        public void pause() {
            myMusicMediaPlayer.pause();
        }

        @Override
        public void stop() {
            stopTheSong();
        }

        @Override
        public void nextSong() {
            stopTheSong();
            playSong(++songPos);
        }

        @Override
        public void prevSong() {
            playSong(--songPos);
        }
    }
}
