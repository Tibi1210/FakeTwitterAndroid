package com.example.fake_twitter_android;

import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Timestamp;
import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity {

    private static final String LOG_TAG = HomePageActivity.class.getName();

    private FirebaseUser user;

    private RecyclerView mRecyclerView;
    private ArrayList<TweetCard> mItemList;
    private TweetCardAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference tweetsCollection;

    private NotificationHandler mNotificationHandler;

    private TextView postUsername;
    private EditText postTweet;
    private ImageView postPfp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        getSupportActionBar().setTitle("");
        setContentView(R.layout.activity_home_page);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.i(LOG_TAG, "Authenticated user.");
        } else {
            Log.e(LOG_TAG, "Unauthenticated user.");
            finish();
        }

        mRecyclerView = findViewById(R.id.recycleViewTweets);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mItemList = new ArrayList<>();
        mAdapter = new TweetCardAdapter(this, mItemList,user);
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        tweetsCollection = mFirestore.collection("Tweets");

        queryData();
        //initData();

        postUsername = findViewById(R.id.TweetPostUsername);
        postUsername.setText(user.getEmail());

        postTweet = findViewById(R.id.TweetPostTweet);
        postPfp = findViewById(R.id.UserPfpTweetPost);

        mNotificationHandler = new NotificationHandler(this);

    }

    private void queryData() {

        mItemList.clear();

        tweetsCollection.orderBy("currentTime", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                TweetCard item = doc.toObject(TweetCard.class);
                item.setId(doc.getId());
                mItemList.add(item);
            }

            if (mItemList.size() == 0) {
                try {
                    initData();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                queryData();
            }
            mAdapter.notifyDataSetChanged();
        });

    }

    public void deleteTweet(TweetCard tweet) {
        DocumentReference ref = tweetsCollection.document(tweet._getId());
        ref.delete().addOnSuccessListener(success -> {
            Log.i(LOG_TAG, "Deleted: " + tweet._getId());
        }).addOnFailureListener(failure -> {
            Toast.makeText(this,"Delete Failed!",Toast.LENGTH_LONG).show();
        });
        queryData();
    }

    private void initData() throws InterruptedException {
        String[] itemUsername = getResources().getStringArray(R.array.names);
        String[] itemTweet = getResources().getStringArray(R.array.tweets);
        TypedArray itemPfp = getResources().obtainTypedArray(R.array.images);

        //mItemList.clear();

        for (int i = 0; i < itemUsername.length; i++) {
            tweetsCollection.add(new TweetCard(itemUsername[i], itemTweet[i], itemPfp.getResourceId(i, 0), new Timestamp(System.currentTimeMillis())));
            sleep(5000);
            //mItemList.add(new TweetCard(itemUsername[i], itemTweet[i], itemPfp.getResourceId(i, 0)));
        }
        itemPfp.recycle();
        //mAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.top_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.top_menu_search);
        SearchView search = (SearchView) MenuItemCompat.getActionView(menuItem);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                mAdapter.getFilter().filter(s);

                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.top_menu_home:
                Log.i(LOG_TAG, "HOME");
                return true;
            case R.id.top_menu_profile:
                Log.i(LOG_TAG, "PROFILE");
                return true;
            case R.id.top_menu_logout:
                Log.i(LOG_TAG, "LOGOUT");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public void tweet(View view) {
        if (!(postTweet.getText().toString().equals(null) || postTweet.getText().toString().equals(""))) {
            tweetsCollection.add(new TweetCard(postUsername.getText().toString(), postTweet.getText().toString(), 2131230854, new Timestamp(System.currentTimeMillis())));
            mNotificationHandler.send("Tweet sent!",postUsername.getText().toString()+" posted: "+postTweet.getText().toString());
            postTweet.setText("");
            queryData();

        }
    }
}