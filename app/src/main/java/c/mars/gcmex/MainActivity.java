package c.mars.gcmex;

import android.app.Activity;
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
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

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
    @OnClick(R.id.b) void b(){
        String message = String.valueOf(new Random().nextInt(100));
        RestAdapter restAdapter=new RestAdapter.Builder()
                .setEndpoint("https://gcm-http.googleapis.com")
                .build();
        GcmApiService service= restAdapter.create(GcmApiService.class);
        rx.Observable<GcmApiService.MessageId> observable= service.send(new GcmApiService.Message("/topics/a", message));
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(messageId -> adapter.add("sent messageId:" + messageId.getMessage_id()), throwable -> adapter.add("error:" + throwable.getMessage()));
    }

    MAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Timber.plant(new Timber.DebugTree());

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

//        data.addAll(Arrays.asList(DATA));
        adapter = new MAdapter();
        recyclerView.setAdapter(adapter);

        boolean sa=checkPlayServices();
        t.setText(sa?"services ok":"no services available");

        if (sa) {
            Intent intent = new Intent(this, RegService.class);
            startService(intent);
        }
    }

    private boolean checkPlayServices() {
        int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        return result == ConnectionResult.SUCCESS;
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

        public void add(String s){
            data.add(s);
            adapter.notifyItemInserted(data.size()-1);
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

    BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.add(intent.getStringExtra(GcmListenerService.EXTRA_MSG));
        }
    };
}
