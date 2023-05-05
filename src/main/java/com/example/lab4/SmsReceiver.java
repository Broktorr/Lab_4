package com.example.lab4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] messages = null;
        String str = "";

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], bundle.getString("format"));
                String messageBody = messages[i].getMessageBody();

                if (i != messages.length - 1) {
                    messageBody += "\n";
                }
                str += "SMS from " + messages[i].getOriginatingAddress();
                str += " :";
                str += messageBody;
            }
            int customDurationInMillis = 1000000;
            showToastWithCustomDuration(context, str, customDurationInMillis);

        }
    }
    private void showToastWithCustomDuration(Context context, String message, int durationInMillis) {
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        new CountDownTimer(durationInMillis, 10000) {
            public void onTick(long millisUntilFinished) {
                toast.show();
            }

            public void onFinish() {
                toast.cancel();
            }
        }.start();
    }
}