package c.mars.gcmex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import timber.log.Timber;

/**
 * Created by Constantine Mars on 8/1/15.
 */
public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {

    public static final String EXTRA_MSG ="EXTRA_MSG";
    public static final String ACTION_MSG="ACTION_MSG";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Timber.d(from + ":" + message);

        Intent i=new Intent(ACTION_MSG);
        i.putExtra(EXTRA_MSG, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}
