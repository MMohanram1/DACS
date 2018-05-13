package online.mmohanram13.dacs;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements ChatFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener,
        DashboardFragment.OnFragmentInteractionListener{

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_chat:
                    selectedFragment = new ChatFragment();
                    break;
                case R.id.navigation_dashboard:
                    selectedFragment = new DashboardFragment();
                    break;
                case R.id.navigation_map:
                    selectedFragment = new MapFragment();
                    break;
            }
            return loadFragment(selectedFragment);
        }
    };

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //loading the default fragment
        loadFragment(new ChatFragment());

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //Leaving it empty
    }
}
