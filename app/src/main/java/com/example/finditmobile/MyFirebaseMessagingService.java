package com.example.finditmobile;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "default_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        String title = msg.getNotification() != null
                ? msg.getNotification().getTitle()
                : msg.getData().get("titulo");
        String body = msg.getNotification() != null
                ? msg.getNotification().getBody()
                : msg.getData().get("mensagem");

        showNotification(
                title != null ? title : "FindIt",
                body  != null ? body  : "Você recebeu uma nova notificação."
        );
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // opcional: gravar em users/{uid}/fcmToken se usuário estiver logado
    }

    private void showNotification(String title, String text) {
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "FindIt Notificações",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(ch);
            }
        }
        Intent it = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                this, 0, it, PendingIntent.FLAG_IMMUTABLE
        );
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pi);
        nm.notify((int)System.currentTimeMillis(), b.build());
    }
}