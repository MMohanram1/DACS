package online.mmohanram13.dacs;

import android.content.Context;
import android.content.Intent;
<<<<<<< HEAD
import android.database.sqlite.SQLiteDatabase;
=======
>>>>>>> 9194d84e39f844186a5713b6a185bb9af3977d8d
import android.media.AudioManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import online.mmohanram13.dacs.database.ConversationDatabaseHelper;

public class MainActivity extends ConnectionsActivity {
    private List<Endpoint> connectionInfoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ConnectionInfoAdapter mAdapter;
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private static final String SERVICE_ID =
            "online.mmohanram13.dacs.SERVICE_ID";
    private String mName;
<<<<<<< HEAD
    protected String recipientText,endpointRecipientId;
    //protected Map<String,String> textMap = new HashMap<>();

    private ConversationDatabaseHelper db;

=======
    private static final String TAG = "ConnectionsActivity" ;
>>>>>>> 9194d84e39f844186a5713b6a185bb9af3977d8d
    /**
     * Listens to holding/releasing the volume rocker.
     */
    private final GestureDetector mGestureDetector =
            new GestureDetector(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP) {
                @Override
                protected void onHold() {
                    logV("onHold");
                    startRecording();
                }

                @Override
                protected void onRelease() {
                    logV("onRelease");
                    stopRecording();
                }
            };

    /**
     * For recording audio as the user speaks.
     */
    @Nullable
    private AudioRecorder mRecorder;

    /**
     * For playing audio from other users nearby.
     */
    @Nullable
    private AudioPlayer mAudioPlayer;

    /**
     * The phone's original media volume.
     */
    private int mOriginalVolume;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (/*mState == State.CONNECTED && */mGestureDetector.onKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Home");

        db = new ConversationDatabaseHelper(this);

        mName = CodenameGenerator.generate();
        ((TextView) findViewById(R.id.nameText)).setText(mName);

        disconnectFromAllEndpoints();
        startDiscovering();
        startAdvertising();

        recyclerView = findViewById(R.id.recycler_view);

        mAdapter = new ConnectionInfoAdapter(connectionInfoList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Endpoint connectionInfo = connectionInfoList.get(position);
                Toast.makeText(getApplicationContext(), connectionInfo.getName() + " is selected!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,ConversationActivity.class);
                intent.putExtra("endpointName",connectionInfo.getName());
                intent.putExtra("endpointId",connectionInfo.getId());
                intent.putExtra("localEndpointName",localEndpointName);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        logD(String.format(" " + getDiscoveredEndpoints()));
        prepareConnectionData();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set the media volume to max.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mOriginalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

    }

    @Override
    protected void onStop() {
        // Restore the original volume.
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);

        // Stop all audio-related threads
        if (isRecording()) {
            stopRecording();
        }
        /*if (isPlaying()) {
            stopPlaying();
        }*/

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAdvertising();
        stopDiscovering();
    }

    private void prepareConnectionData(){
        logD("Set Values: " + getConnectedEndpoints());
        connectionInfoList.addAll(getConnectedEndpoints());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected String getName() {
        return mName;
    }

    @Override
    public String getServiceId() {
        return SERVICE_ID;
    }

    @Override
    public Strategy getStrategy() {
        return STRATEGY;
    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        // We found an advertiser!
        if(!isConnected(endpoint)){
            connectToEndpoint(endpoint);
        }
    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, com.google.android.gms.nearby.connection.ConnectionInfo connectionInfo) {
        // We accept the connection immediately.
        if(!isConnected(endpoint)){
            acceptConnection(endpoint);
        }
    }

    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_connected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        prepareConnectionData();
        try {
            sendAll(Payload.fromBytes("Text from Main Activity".getBytes("UTF-8")));
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_disconnected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        prepareConnectionData();
    }

    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
<<<<<<< HEAD
        if (payload.getType() == Payload.Type.BYTES) {
            try {
                recipientText = new String(payload.asBytes(), "UTF-8");
                endpointRecipientId = endpoint.getId();
                //textMap.put(endpointRecipientId,recipientText);
                logD("Log from Main Activity: " + recipientText + " " + endpointRecipientId);
                /*Set keys = textMap.keySet();
                for (Iterator i = keys.iterator(); i.hasNext(); ) {
                    String key = (String) i.next();
                    String value = textMap.get(key);
                    logD(key + " ; " + value);
                }*/
                long id = db.insertConverseData(endpoint.getName(), localEndpointName,recipientText);
                logD("Sender: " + endpoint.getName() + " Recipient: " + localEndpointName + " Data: " + recipientText);
=======
        super.onReceive(endpoint, payload);
        if (payload.getType() == Payload.Type.BYTES) {
            try {
                String receiveText = new String(payload.asBytes(), "UTF-8");
                logD("Log from Main Activity: " + receiveText);
>>>>>>> 9194d84e39f844186a5713b6a185bb9af3977d8d
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        else if(payload.getType() == Payload.Type.STREAM) {
            logD("onReceive code for playing recorded stream");
            if (payload.getType() == Payload.Type.STREAM) {
                if (mAudioPlayer != null) {
                    mAudioPlayer.stop();
                    mAudioPlayer = null;
                }

                AudioPlayer player =
                        new AudioPlayer(payload.asStream().asInputStream()) {
                            @WorkerThread
                            @Override
                            protected void onFinish() {
                                runOnUiThread(
                                        new Runnable() {
                                            @UiThread
                                            @Override
                                            public void run() {
                                                mAudioPlayer = null;
                                            }
                                        });
                            }
                        };
                mAudioPlayer = player;
                player.start();
            }
        }
    }

    /**
     * Stops all currently streaming audio tracks.
     */
    private void stopPlaying() {
        logV("stopPlaying()");
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
            mAudioPlayer = null;
        }
    }

    /**
     * @return True if currently playing.
     */
    private boolean isPlaying() {
        return mAudioPlayer != null;
    }

    /**
     * Starts recording sound from the microphone and streaming it to all connected devices.
     */
    private void startRecording() {
        logV("startRecording()");
        try {
            ParcelFileDescriptor[] payloadPipe = ParcelFileDescriptor.createPipe();

            // Send the first half of the payload (the read side) to Nearby Connections.
            sendAll(Payload.fromStream(payloadPipe[0]));

            // Use the second half of the payload (the write side) in AudioRecorder.
            mRecorder = new AudioRecorder(payloadPipe[1]);
            mRecorder.start();
        } catch (IOException e) {
            logE("startRecording() failed", e);
        }
    }

    /**
     * Stops streaming sound from the microphone.
     */
    private void stopRecording() {
        logV("stopRecording()");
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder = null;
        }
    }

    /**
     * @return True if currently streaming from the microphone.
     */
    private boolean isRecording() {
        return mRecorder != null && mRecorder.isRecording();
    }

    @Override
    protected void onConnectionFailed(Endpoint endpoint) {
    }

    @Override
    protected void logD(String msg) {
        super.logD(msg);
    }
}
