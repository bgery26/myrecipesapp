package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class RecipeListActivity extends AppCompatActivity {
    private static final String LOG_TAG = RecipeListActivity.class.getName();

    private FirebaseUser user;
    private FirebaseAuth mAuth;

    private RecyclerView mRecyclerview;
    private ArrayList<RecipeItem> mItemlist;
    private RecipeItemAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;
    private NotificationHandler mNotificationHandler;
    private AlarmManager mAlarmManager;



    private int gridNumber = 1;
    private int queryLimit = 10;
    private boolean viewRow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);


        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            Log.d(LOG_TAG,"Authenticated user!");
        } else {
            Log.d(LOG_TAG,"Unauthenticated user!");
            finish();
        }



        mRecyclerview = findViewById(R.id.recyclerView);
        mRecyclerview.setLayoutManager(new GridLayoutManager(this,gridNumber));
        mItemlist = new ArrayList<>();

        mAdapter = new RecipeItemAdapter(this,mItemlist);

        mRecyclerview.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");

        queryData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver,filter);

        mNotificationHandler = new NotificationHandler(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        setAlarmManager();

    }

    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action == null)
                return;
            switch (action) {
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 10;
                    break;

                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 5;
                    break;
            }
            queryData();

        }
    };
    private void queryData() {
        mItemlist.clear();

        //mItems.whereEqualTo()..
        mItems.orderBy("name").limit(queryLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot document : queryDocumentSnapshots) {
                RecipeItem item = document.toObject(RecipeItem.class);
                item.setId(document.getId());
                mItemlist.add(item);
            }

            if(mItemlist.size()==0) {
                intializeData();
                queryData();
            }
            mAdapter.notifyDataSetChanged();
        });

    }

    public void deleteItem(RecipeItem item) {
        DocumentReference ref = mItems.document(item._getId());
        ref.delete().addOnSuccessListener(success -> {
            Log.d(LOG_TAG, "Item is succesfully deleted: " + item._getId());
        }).addOnFailureListener(failure -> {
            Toast.makeText(this,"Item " + item._getId() + " cannot be deleted.", Toast.LENGTH_LONG).show();

        });

        mNotificationHandler.send(item.getName());
        queryData();

    }


    private void intializeData() {
        String[] itemsList = getResources().getStringArray(R.array.recipe_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.recipe_item_desc);
        String[] itemsTime = getResources().getStringArray(R.array.recipe_item_time);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.recipe_item_images);



        for(int i = 0; i < itemsList.length; i++) {
            mItems.add(new RecipeItem(itemsList[i],itemsInfo[i],itemsTime[i],itemsImageResource.getResourceId(i,0)));

        }

        itemsImageResource.recycle();


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.recipe_list_menu,menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout_button:
                Log.d(LOG_TAG,"Log out clicked!");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;

            case R.id.view_selector:
                Log.d(LOG_TAG,"View selector clicked!");
                if(viewRow) {
                    changeSpanCount(item,R.drawable.baseline_grid_view_24,1);

                }else{
                    changeSpanCount(item,R.drawable.baseline_view_stream_24,2);

                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerview.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }

    private void setAlarmManager() {
        long repeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        Intent intent = new Intent(this,AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerTime,repeatInterval,pendingIntent);

        //mAlarmManager.cancel(pendingIntent);

    }
}