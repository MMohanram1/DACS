package online.mmohanram13.dacs;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
import java.util.Map;
import java.util.TimeZone;

import online.mmohanram13.dacs.database.model.ConversationDB;
import online.mmohanram13.dacs.database.ConversationDatabaseHelper;
import online.mmohanram13.dacs.recyclerchat.ChatData;
import online.mmohanram13.dacs.recyclerchat.ConversationRecyclerView;


public class ConversationActivity extends ConnectionsActivity {
    private RecyclerView mRecyclerView;
    private ConversationRecyclerView mAdapter;
    private EditText text;
    private Button send;
    String value, endpointId, localName;
    String localTime, presentDate1;
    String presentDate = new String("Null");
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private static final String SERVICE_ID =
            "online.mmohanram13.dacs.SERVICE_ID";
    private String mName;
    private ConversationDatabaseHelper db;
    private List<ConversationDB> chatDataList = new ArrayList<>();

    @Override
    protected void onStart() {
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
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable,1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        db = new ConversationDatabaseHelper(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            value = extras.getString("endpointName");
            endpointId = extras.getString("endpointId");
            localName = extras.getString("localEndpointName");
            //The key argument here must match that used in the other activity
        }
        ActionBar ab = getSupportActionBar();
        logD(value + " " + endpointId);
        ab.setTitle(value);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ConversationRecyclerView(this, setData());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
            }
        }, 1000);

        text = findViewById(R.id.et_message);
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
        send = findViewById(R.id.bt_send);
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
    }

    public List<ChatData> setData(){
        List<ChatData> data = new ArrayList<>();

        String text[] = {"Chat, Share or Talk"};
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
        logD("onReceiveData() of ConversationActivity");
        final ConversationDB conversationDB = conversationDB_chat;
        runOnUiThread(new Runnable() {
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
            logD("Runnable Entered");
            chatDataList.addAll(db.getAllUnreadData(value));

            for(ConversationDB conversationDB:chatDataList){
                db.updateData(conversationDB);
                logD("Id = " + conversationDB.getId() + " Sender: " + conversationDB.getSender() + " Recipient: " + conversationDB.getRecipient() + " Data: " + conversationDB.getData() + " Viewed: " + conversationDB.getViewed() + " TimeStamp: " + conversationDB.getTimestamp());
                setRecipientData(conversationDB);
            }

            handler.postDelayed(runnable,1000);
        }
    };

    Handler handler=new Handler();

}
