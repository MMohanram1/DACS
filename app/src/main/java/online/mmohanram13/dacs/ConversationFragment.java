package online.mmohanram13.dacs;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import online.mmohanram13.dacs.database.ConversationDatabaseHelper;
import online.mmohanram13.dacs.database.model.ConversationDB;
import online.mmohanram13.dacs.recyclerchat.ChatData;
import online.mmohanram13.dacs.recyclerchat.ConversationRecyclerView;


public class ConversationFragment extends ConnectionsFragment {
    private RecyclerView mRecyclerView;
    private ConversationRecyclerView mAdapter;
    private EditText text;
    private Button send;
    String localTime, presentDate1;
    public String value,endpointId,localName;
    String presentDate = new String("Null");
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private static final String SERVICE_ID =
            "online.mmohanram13.dacs.SERVICE_ID";
    private String mName;
    private ConversationDatabaseHelper db;
    private List<ConversationDB> chatDataList = new ArrayList<>();
    private OnFragmentInteractionListener mListener;

    private static final String ARG_PARAM1 = "endpointName";
    private static final String ARG_PARAM2 = "endpointId";
    private static final String ARG_PARAM3 = "localEndpointName";

    public ConversationFragment(){
        //Required empty constructor
    }

    public static ConversationFragment newInstance(String value1, String endpointId1, String localName1) {
        Log.d("Conversation Fragment", "newInstance: " + value1 + " " + endpointId1 + " " + localName1);
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM1,value1);
        bundle.putString(ARG_PARAM2,endpointId1);
        bundle.putString(ARG_PARAM3,localName1);
        Log.d("Conversation Fragment", "args: " + ARG_PARAM1 + " " + ARG_PARAM2 + " " + ARG_PARAM3);
        ConversationFragment conversationFragment = new ConversationFragment();
        conversationFragment.setArguments(bundle);
        return conversationFragment;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected String getName() {
        return mName;
    }

    @Override
    protected String getServiceId() {
        return SERVICE_ID;
    }

    @Override
    protected Strategy getStrategy() {
        return STRATEGY;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnable,500);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        db = new ConversationDatabaseHelper(getActivity());

        Bundle bundle = getArguments();
        if (getArguments() != null) {
            value = bundle.getString(ARG_PARAM1);
            endpointId = bundle.getString(ARG_PARAM2);
            localName = bundle.getString(ARG_PARAM3);
        }
        logD("Conversation Fragment: eN:" + value + " eId:" + endpointId + " leN:" + localName);

        ((ConversationActivity) getActivity()).setActionBarTitle(value);

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ConversationRecyclerView(getActivity(), setData());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
            }
        }, 1000);

        text = view.findViewById(R.id.et_message);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                    }
                }, 500);
            }
        });
        send = view.findViewById(R.id.bt_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!text.getText().equals("")) {
                    List<ChatData> data = new ArrayList<>();
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
                    Date currentTimeStamp = cal.getTime();
                    DateFormat date = new SimpleDateFormat("EEE MMM d", Locale.getDefault());
                    DateFormat time = new SimpleDateFormat("h:mm a", Locale.getDefault());
                    date.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
                    time.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
                    presentDate1 = date.format(currentTimeStamp);
                    if (!presentDate.equals(presentDate1)) {
                        presentDate = presentDate1;
                        ChatData item = new ChatData();
                        item.setType("0");
                        item.setText(presentDate);
                        item.setTime("");
                        data.add(item);
                    }
                    localTime = time.format(currentTimeStamp);
                    ChatData item = new ChatData();
                    item.setTime(localTime);
                    item.setType("2");
                    item.setText(text.getText().toString());
                    data.add(item);
                    mAdapter.addItem(data);
                    mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                    try {
                        logD("Sending Payload: EndpointId = " + endpointId + " ;String = " + text.getText().toString());
                        send(Payload.fromBytes(text.getText().toString().getBytes("UTF-8")), endpointId);
                        createData(localName,value,text.getText().toString());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    text.setText("");
                }
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public List<ChatData> setData(){
        List<ChatData> data = new ArrayList<>();

        String text[] = {"You can chat here"};
        String time[] = {""};
        String type[] = {"0"};

        for (int i=0; i<text.length; i++){
            ChatData item = new ChatData();
            item.setType(type[i]);
            item.setText(text[i]);
            item.setTime(time[i]);
            data.add(item);
        }
        return data;
    }

    private void createData(String sender, String recipient, String data){
        long id = db.insertConverseData(sender, recipient, data);
        logD("Sender: " + sender + " Recipient: " + recipient + " Data: " + data);
    }

    public void setRecipientData(ConversationDB conversationDB_chat) {
        logD("onReceiveData() of ConversationFragment");
        final ConversationDB conversationDB = conversationDB_chat;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!conversationDB.getData().equals("")) {
                        logD(conversationDB.getData() + " " + conversationDB.getSender());
                        List<ChatData> data = new ArrayList<>();
                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
                        Date currentTimeStamp = cal.getTime();
                        DateFormat date = new SimpleDateFormat("EEE MMM d", Locale.getDefault());
                        date.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
                        presentDate1 = date.format(currentTimeStamp);
                        if (!presentDate.equals(presentDate1)) {
                            presentDate = presentDate1;
                            ChatData item = new ChatData();
                            item.setType("0");
                            item.setText(presentDate);
                            item.setTime("");
                            data.add(item);
                        }
                        ChatData item = new ChatData();
                        item.setTime(conversationDB.getTimestamp());
                        item.setType("1");
                        item.setText(conversationDB.getData());
                        data.add(item);
                        mAdapter.addItem(data);
                        mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                        chatDataList.remove(conversationDB);
                    }
                } catch (NullPointerException e) {
                    e.getCause();
                }
            }
        });
    }

    @Override
    protected void logD(String msg) {
        super.logD(msg);
    }

    /**
     * Joins 2 arrays together.
     */
    private static String[] join(String[] a, String... b) {
        String[] join = new String[a.length + b.length];
        System.arraycopy(a, 0, join, 0, a.length);
        System.arraycopy(b, 0, join, a.length, b.length);
        return join;
    }

    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            chatDataList.addAll(db.getAllUnreadData(value));

            for(ConversationDB conversationDB:chatDataList){
                db.updateData(conversationDB);
                logD("Id = " + conversationDB.getId() + " Sender: " + conversationDB.getSender() + " Recipient: " + conversationDB.getRecipient() + " Data: " + conversationDB.getData() + " Viewed: " + conversationDB.getViewed() + " TimeStamp: " + conversationDB.getTimestamp());
                setRecipientData(conversationDB);
            }

            handler.postDelayed(runnable,500);
        }
    };

    Handler handler=new Handler();

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
