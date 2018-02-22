package com.proj.abhi.mytermplanner.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.proj.abhi.mytermplanner.R;
import com.proj.abhi.mytermplanner.utils.Constants;
import com.proj.abhi.mytermplanner.utils.DBOpenHelper;

public class NotifyService extends Service {

    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
        }
    }

    public static final String INTENT_NOTIFY = "com.proj.abhi.mytermmanger.services.INTENT_NOTIFY";
    private NotificationManager mNM;
 
    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
 
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getBooleanExtra(INTENT_NOTIFY, false))
            showNotification(intent);

        return START_NOT_STICKY;
    }
 
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new ServiceBinder();

    private void showNotification(Intent userIntent) {
        long time = System.currentTimeMillis();
        Bundle b=userIntent.getBundleExtra(Constants.PersistAlarm.USER_BUNDLE);
        String contextText = "Event: "+b.getString(Constants.PersistAlarm.CONTENT_TEXT)
                +" for "+b.get(Constants.PersistAlarm.CONTENT_TITLE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this);

        Intent sendingIntent=b.getParcelable(Constants.CURRENT_INTENT);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(),sendingIntent, 0);

        mBuilder.setContentIntent(pendingIntent);

        mBuilder.setSmallIcon(getNotificationIcon());
        mBuilder.setContentTitle(Constants.APP_NAME+": "+b.getString(Constants.PersistAlarm.USER_OBJECT));
        mBuilder.setContentText(contextText);
        mBuilder.setAutoCancel(true);
        mBuilder.setWhen(time);
        mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        mBuilder.setVibrate(new long[] {0, 500, 200,500 });
        mBuilder.setLights(Color.MAGENTA, 500, 500);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));


        int id = b.getInt(Constants.Ids.ALARM_ID);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(id, mBuilder.build());
        deleteInDb(id);
        stopSelf();
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_stat_book : R.mipmap.ic_launcher;
    }

    private void deleteInDb(int id){
        DBOpenHelper helper = new DBOpenHelper(this);
        SQLiteDatabase database = helper.getWritableDatabase();
        String where=Constants.ID+"="+id;
        database.delete(Constants.Tables.TABLE_PERSIST_ALARM,where,null);
        database.close();
    }
}
