package c.mars.gcmex;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Constantine Mars on 8/1/15.
 */
public class IDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegService.class);
        startService(intent);
    }
}
