package c.mars.gcmex;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.RestAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;


public class MainActivity extends Activity {

    @Bind(R.id.l)
    RecyclerView recyclerView;
    @Bind(R.id.t)
    TextView t;
    @Bind(R.id.b)
    Button b;
    @Bind(R.id.i)
    TextView i;

    @OnClick(R.id.b)
    void b() {
        String message = String.valueOf(new Random().nextInt(100));
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://gcm-http.googleapis.com")
                .build();
        GcmApiService service = restAdapter.create(GcmApiService.class);
        rx.Observable<GcmApiService.MessageId> observable = service.send(new GcmApiService.Message("/topics/a", message));
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(messageId -> adapter.add("message \"" + message + "\" sent [id:" + messageId.getMessage_id() + "]"), throwable -> adapter.add("error:" + throwable.getMessage()));
    }

    MAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Timber.plant(new Timber.DebugTree());

        b.setEnabled(false);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MAdapter();
        recyclerView.setAdapter(adapter);

        if (checkPlayServices()) {
            startGcmService();
        }
    }

    private void startGcmService() {
        Intent intent = new Intent(this, RegService.class);
        startService(intent);
        b.setEnabled(true);
        i.setVisibility(View.VISIBLE);
    }

    private final static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 2;

    private boolean checkPlayServices() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                GooglePlayServicesUtil.getErrorDialog(status, this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
            } else {
                String s = "This device is not supported";
                t.setText(s);
                Toast.makeText(this, s, Toast.LENGTH_LONG).show();
            }
            t.setText("GooglePlayServices... waiting solution");
            i.setVisibility(View.GONE);
            return false;
        }
        t.setText("");
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if (resultCode == RESULT_CANCELED) {
                    String s = "Google Play Services must be installed for this demo";
                    t.setText(s);
                    Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
                } else if (resultCode == RESULT_OK) {
                    if (checkPlayServices()) {
                        startGcmService();
                    }
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class MAdapter extends RecyclerView.Adapter<MAdapter.ViewHolder> {
        ArrayList<String> data = new ArrayList<>();

        public MAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                this.textView = (TextView) itemView.findViewById(R.id.text);
            }
        }

        public void add(String s) {
            data.add(s);
            int p = data.size() - 1;
            adapter.notifyItemInserted(p);
            recyclerView.scrollToPosition(p);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(GcmListenerService.ACTION_MSG));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onPause();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.add("received: " + intent.getStringExtra(GcmListenerService.EXTRA_MSG));
            i.setVisibility(View.GONE);
        }
    };
}
