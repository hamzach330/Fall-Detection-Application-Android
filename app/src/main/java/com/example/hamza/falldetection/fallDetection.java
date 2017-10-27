package com.example.hamza.falldetection;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import java.util.logging.LogRecord;

/**
 * Created by HAMZA on 8/26/2015.
 */
public class fallDetection extends Activity implements SensorEventListener {



    private static int NOTIFICATION_ID = 1;

    private SensorManager mSM;
    private  Sensor acce;
    private boolean min;
    private boolean max;
    private int i=0;

    AlertDialog popup;
    Context context = this;

    boolean isAlive = false;
    private long last_time =0;

    Runnable runnable;
    Ringtone ring;
    Uri notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.fall_detection);



          notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
          ring = RingtoneManager.getRingtone(getApplicationContext(), notification);
        mSM = (SensorManager) getSystemService(SENSOR_SERVICE);
        acce = mSM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSM.registerListener((SensorEventListener) this, acce, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        if (sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            double gvt=SensorManager.STANDARD_GRAVITY;
            TextView txt_fall = (TextView)findViewById(R.id.txt_fall);

            float vals[] = sensorEvent.values;
            //int sensor=arg0.sensor.getType();
            double xx=sensorEvent.values[0];
            double yy=sensorEvent.values[1];
            double zz=sensorEvent.values[2];
            double aaa=Math.round(Math.sqrt(Math.pow(xx, 2)
                    +Math.pow(yy, 2)
                    +Math.pow(zz, 2)));


            txt_fall.setTextColor(Color.BLACK);
            if (aaa<=6.0) {
                min=true;
                //mintime=System.currentTimeMillis();
            }

            if (min==true) {
                i++;
                if(aaa>=20) {
                    max=true;
                }
            }

            if (min==true && max==true) {


                txt_fall.setTextColor(Color.RED);
                //Toast.makeText(context, "FALL DETECTED!!!!!", Toast.LENGTH_SHORT).show();

                 checkResponse();
                ring.play();


                i=0;
                min=false;
                max=false;
            }

            if (i>4) {
                i=0;
                min=false;
                max=false;
            }
        }

    }


    private void showNotification(Context con, String title, String content) {

        Intent notificationIntent = new Intent(con, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(con, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(con)
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent);
        Notification notification = mBuilder.build();

        // default phone settings for notifications
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;

        // cancel notification after click
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // show scrolling text on status bar when notification arrives
        notification.tickerText = "Fall Detected !! ";

        // notifiy the notification using NotificationManager
        NotificationManager notificationManager = (NotificationManager) con
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    public  void checkResponse()
    {

        handler.postDelayed(r, 10000);

        AlertDialog.Builder popup_alert = new AlertDialog.Builder(context);

        popup_alert.setMessage("You Have Fallen Down");
        popup_alert.setCancelable(true);

        popup_alert.setPositiveButton("No, I am Fine !!! ",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        Toast.makeText(context, "Emergency not Called", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        isAlive = true;
                        handler.removeCallbacks(r);
                        ring.stop();
                        // handler.removeCallbacks(null);
                        // handler.removeCallbacksAndMessages(handler);


                    }
                });


            popup = popup_alert.create();
            popup.show();

    }


    Handler handler=new Handler();
    Runnable r = new Runnable(){
        public void run() {


            popup.dismiss();
            popup.cancel();
            ring.stop();
            Toast.makeText(context, "Emergency Called", Toast.LENGTH_LONG).show();


            isAlive = false;
            handler.removeCallbacks(r);

        }
    };

    @Override
    protected void onPause() {


        super.onPause();
//        if (dialog != null && dialog.isShowing()) {
//            dialog.dismiss();
//        }


    }
}
