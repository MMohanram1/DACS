package online.mmohanram13.dacs;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import online.mmohanram13.dacs.database.ConversationDatabaseHelper;

public class ChatFragment extends ConnectionsFragment {
    private List<Endpoint> connectionInfoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ConnectionInfoAdapter mAdapter;
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private static final String SERVICE_ID =
            "online.mmohanram13.dacs.SERVICE_ID";
    private String mName;
    protected String recipientText,endpointRecipientId;
    boolean talkieClicked = false;

    private OnFragmentInteractionListener mListener;

    private ConversationDatabaseHelper db;

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    public ChatFragment(){
        //Required empty public constructor
    }

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        db = new ConversationDatabaseHelper(getActivity());

        mName = CodenameGenerator.generate();
        ((TextView) view.findViewById(R.id.nameText)).setText(mName);

        disconnectFromAllEndpoints();
        startDiscovering();
        startAdvertising();

        TextView tv = view.findViewById(R.id.mic_text);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!talkieClicked){
                    startRecording();
                    talkieClicked = true;
                }else{
                    stopRecording();
                    talkieClicked = false;
                }
            }
        });

        recyclerView = view.findViewById(R.id.recycler_view);

        mAdapter = new ConnectionInfoAdapter(connectionInfoList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Endpoint connectionInfo = connectionInfoList.get(position);
                Toast.makeText(getActivity().getApplicationContext(), connectionInfo.getName() + " is selected!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),ConversationActivity.class);
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
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        ((MainActivity) getActivity()).setActionBarTitle("Chat");
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set the media volume to max.
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mOriginalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        logD("onStart() executed successfully");

    }

    @Override
    public void onStop() {
        // Restore the original volume.
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
        getActivity().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);

        // Stop all audio-related threads
        if (isRecording()) {
            stopRecording();
        }
        if (isPlaying()) {
            stopPlaying();
        }

        logD("onStop() execute successfully");

        super.onStop();
    }

    @Override
    public void onDestroy() {
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
                getActivity(), getString(R.string.toast_connected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        prepareConnectionData();
    }

    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {
        Toast.makeText(
                getActivity(), getString(R.string.toast_disconnected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        prepareConnectionData();
    }

    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
        if (payload.getType() == Payload.Type.BYTES) {
            try {
                recipientText = new String(payload.asBytes(), "UTF-8");
                endpointRecipientId = endpoint.getId();
                logD("Log from Main Activity: " + recipientText + " " + endpointRecipientId);
                long id = db.insertConverseData(endpoint.getName(), localEndpointName,recipientText);
                logD("Sender: " + endpoint.getName() + " Recipient: " + localEndpointName + " Data: " + recipientText);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.getCause();
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
                                getActivity().runOnUiThread(
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
            logD("Pipe created successfully");

            // Send the first half of the payload (the read side) to Nearby Connections.
            sendAll(Payload.fromStream(payloadPipe[0]));
            logD("Sent successfully");

            // Use the second half of the payload (the write side) in AudioRecorder.
            mRecorder = new AudioRecorder(payloadPipe[1]);
            logD("AudioRecorder object called successfully");
            mRecorder.start();
            logD("startRecording() started successfully");
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

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
