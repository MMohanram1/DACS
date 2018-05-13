package online.mmohanram13.dacs;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class ConversationActivity extends AppCompatActivity implements ConversationFragment.OnFragmentInteractionListener{

    String value, endpointId, localName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            value = extras.getString("endpointName");
            endpointId = extras.getString("endpointId");
            localName = extras.getString("localEndpointName");
            //The key argument here must match that used in the other activity
            Log.d("Conversation Activity", "onCreate: endpointName="+value+" endpointId="+endpointId+" localEndpointName="+localName);
        }
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, ConversationFragment.newInstance(value, endpointId, localName),"ConversationFragment").commit();
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //Leaving it empty
    }
}
