package com.singularity.trackmyvehicle.fcm;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.singularity.trackmyvehicle.R;
import com.singularity.trackmyvehicle.VehicleTrackApplication;
import com.singularity.trackmyvehicle.di.AppComponent;
import com.singularity.trackmyvehicle.preference.AppPreference;
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository;
import com.singularity.trackmyvehicle.view.activity.SplashScreenActivity;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.inject.Inject;

/**
 * Created by Sadman Sarar on 3/19/18.
 */

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    public     FCMRepository  fcmRepository;
    
    @Inject
    public PrefRepository mPrefRepository;
    
    @Inject
    public AppPreference mAppPreference;

    @Override
    public void onCreate() {
        super.onCreate();
        AppComponent appComponent = VehicleTrackApplication.Companion.getAppComponent();
        if (appComponent != null) {
            appComponent.inject(this);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        Log.e(TAG, "onNewToken: ============");
	    mPrefRepository.saveUnsetFCMToken(s);
//        fcmRepository.postToken(s);
	
	    if (fcmRepository.shouldSendFCMToken() && mAppPreference.getBoolean(AppPreference.isNotificationEnable)) {
		    fcmRepository.postToken(mPrefRepository.unsentFCMToken());
	    }
    }

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

	    if (remoteMessage.getNotification() != null) {
//			sendNotification(remoteMessage.getMessages().getBody(), getString(R.string.app_name));
            sendNotification(remoteMessage.getNotification());
            return;
        }

        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            sendNotification(data.get("title"), data.get("message"));
        }

    }

    private void sendNotification(@NonNull RemoteMessage.Notification remoteNotification) {
        int notifyID = 12341;
        String CHANNEL_ID =
                "com.singularity.trackmyvehicle.default_notification_channel_id";//getString(R.string.default_notification_channel_id);
        CharSequence name =
                "com.singularity.trackmyvehicle.default_notification_channel_id";//getString(R.string.default_notification_channel_id);// The user-visible name of the channel.

        // Create a notification and set the notification channel.
        Notification notification;
        Uri imageUrl = remoteNotification.getImageUrl();
        NotificationCompat.Style style = null;
        Bitmap bitmap = null;
        if (imageUrl != null) {
            try {
                bitmap = Picasso.get().load(imageUrl).get();
                style = new NotificationCompat.BigPictureStyle()
                        .setBigContentTitle(remoteNotification.getTitle())
                        .bigLargeIcon(null)
                        .bigPicture(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                style = new NotificationCompat.BigTextStyle()
                        .bigText(remoteNotification.getBody())
                        .setBigContentTitle(remoteNotification.getTitle())
                        .setSummaryText(remoteNotification.getBody());
            }

        } else {
            style = new NotificationCompat.BigTextStyle()
                    .bigText(remoteNotification.getBody())
                    .setBigContentTitle(remoteNotification.getTitle())
                    .setSummaryText(remoteNotification.getBody());
        }

        Intent intent = new Intent(this, SplashScreenActivity.class);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        Random generator = new Random();
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, generator.nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

		/*Uri soundUri = Uri.parse(
				ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.slow_spring_board);*/

        long[] vibrationPattern =
                {0, 500, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000, 500, 1000};
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(remoteNotification.getTitle())
                        .setContentText(remoteNotification.getBody())
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setStyle(style)
                        .setAutoCancel(true)
//				.setSound(soundUri)
                        .setVibrate(vibrationPattern)
                        .setContentIntent(pendingIntent)
//                    .addAction(R.drawable.ic_done_black_24dp, "Approve", pendingIntent)
//                    .addAction(R.drawable.ic_close_black_24dp, "Reject", pendingIntent)
                        .setSmallIcon(R.drawable.ic_app_logo);

        if (bitmap != null) {
            notificationBuilder.setLargeIcon(bitmap);
        }

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            NotificationChannel mChannel;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
//			mChannel.setSound(soundUri, attributes);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(vibrationPattern);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        notification = notificationBuilder.build();

        if (mNotificationManager != null) {
            mNotificationManager.notify(notifyID, notification);
        }
    }

    @SuppressLint("WrongConstant")
    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

       PendingIntent pendingIntent = PendingIntent.getActivity(this, 0  /*Request code*/ , intent,
                PendingIntent.FLAG_ONE_SHOT
        );




        String channelId = "tmv-robi-channe";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setLargeIcon(bm)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long[] vibrationPatter = new long[]{0, 500, 500, 500, 500, 500, 500, 500, 500};
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            channel.setSound(uri, new AudioAttributes.Builder().build());
            channel.enableVibration(true);
            channel.setVibrationPattern(vibrationPatter);
            channel.setShowBadge(false);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }
}