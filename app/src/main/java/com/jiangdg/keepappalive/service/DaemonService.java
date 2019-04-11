package com.jiangdg.keepappalive.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jiangdg.keepappalive.R;
import com.jiangdg.keepappalive.utils.Contants;
import com.jiangdg.keepappalive.utils.LocationUtils;

import java.util.Timer;
import java.util.TimerTask;

/**前台Service，使用startForeground
 * 这个Service尽量要轻，不要占用过多的系统资源，否则
 * 系统在资源紧张时，照样会将其杀死
 *
 * Created by jianddongguo on 2017/7/7.
 * http://blog.csdn.net/andrexpert
 */
public class DaemonService extends Service {
    private static final String TAG = "DaemonService";
    public static final int NOTICE_ID = 100;
    private int timeSec;
    private int timeMin;
    private int timeHour;
    private Timer mRunTimer;
    private boolean hasOne = true;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(Contants.DEBUG)
            Log.d(TAG,"DaemonService---->onCreate被调用，启动前台service");
        //如果API大于18，需要弹出一个可见通知
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle("KeepAppAlive");
            builder.setContentText("DaemonService is runing...");
            startForeground(NOTICE_ID,builder.build());
            // 如果觉得常驻通知栏体验不好
            // 可以通过启动CancelNoticeService，将通知移除，oom_adj值不变
            Intent intent = new Intent(this,CancelNoticeService.class);
            startService(intent);
        }else{
            startForeground(NOTICE_ID,new Notification());
        }
//        if(hasOne){
//            hasOne = false;
//            startRunTimer();
//        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 如果Service被终止
        // 当资源允许情况下，重启service

        return START_STICKY;
    }

    private void startRunTimer() {
        TimerTask mTask = new TimerTask() {
            @Override
            public void run() {
                timeSec++;
                if(timeSec == 60){
                    timeSec = 0;
                    timeMin++;
                }
                if(timeMin == 60){
                    timeMin = 0;
                    timeHour++;
                }
                if(timeHour == 24){
                    timeSec = 0;
                    timeMin = 0;
                    timeHour = 0;
                }
                if(timeSec % 5 ==0 ){
                    Log.e(TAG,"获取成功,后台还活着");
                    //初始化
                    LocationUtils.initLocation(getApplicationContext());
                    Log.e(TAG,"经度："+LocationUtils.longitude);
                    Log.e(TAG,"纬度："+LocationUtils.latitude);
//                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                    Ringtone mRingtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
//                    mRingtone.play();
                }
            }
        };
        mRunTimer = new Timer();
        // 每隔1s更新一下时间
        mRunTimer.schedule(mTask,1000,1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 如果Service被杀死，干掉通知
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            NotificationManager mManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            mManager.cancel(NOTICE_ID);
        }
        if(Contants.DEBUG)
            Log.d(TAG,"DaemonService---->onDestroy，前台service被杀死");
        // 重启自己
        Intent intent = new Intent(getApplicationContext(),DaemonService.class);
        startService(intent);
    }
}
