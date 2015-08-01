package c.mars.gcmex;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;


public class RegService extends IntentService {

    private static final String[] TOPICS = {"a"};

    public RegService() {
        super(RegService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            SharedPreferences p= PreferenceManager.getDefaultSharedPreferences(this);
            synchronized (this){
                InstanceID instanceID=InstanceID.getInstance(this);
                String senderId=getString(R.string.gcm_defaultSenderId);
                try {
                    String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    subscribeTopics(token);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}
