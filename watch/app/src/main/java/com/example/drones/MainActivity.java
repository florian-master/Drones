package com.example.drones;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import androidx.wear.widget.drawer.WearableNavigationDrawerView;
import androidx.wear.widget.drawer.WearableNavigationDrawerView.WearableNavigationDrawerAdapter;

public class MainActivity extends WearableActivity implements
        WearableNavigationDrawerView.OnItemSelectedListener {

    private MenuFragment MenuFragment;
    private String[] menus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        menus = getResources().getStringArray(R.array.menus);

        MenuFragment = new MenuFragment();
        Bundle args = new Bundle();
        MenuFragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, MenuFragment).commit();
        WearableNavigationDrawerView mWearableNavigationDrawer = findViewById(R.id.top_navigation_drawer);
        mWearableNavigationDrawer.setAdapter(new NavigationAdapter());
        mWearableNavigationDrawer.getController().peekDrawer();
        mWearableNavigationDrawer.addOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(int position) {
        MenuFragment.changeView(getLayoutInflater(), position, menus);

    }

    private final class NavigationAdapter extends WearableNavigationDrawerAdapter {
        private String[] menus;

        NavigationAdapter() {
            menus = getResources().getStringArray(R.array.menus);
        }

        @Override
        public int getCount() {
            return menus.length;
        }

        @Override
        public String getItemText(int pos) {
            int resourceName = getResources().getIdentifier(menus[pos], "array", getPackageName());
            return getResources().getStringArray(resourceName)[1];
        }

        @Override
        public Drawable getItemDrawable(int pos) {
            return null;
        }
    }


}
