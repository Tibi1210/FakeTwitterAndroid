package com.example.fake_twitter_android;

import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Timestamp;
import java.util.ArrayList;

public class ProfilePageActivity extends AppCompatActivity {

    private static final String LOG_TAG = ProfilePageActivity.class.getName();

    private FirebaseUser user;

    private RecyclerView mRecyclerView;
    private ArrayList<TweetCard> mItemList;
    private TweetCardAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference tweetsCollection;
    private CollectionReference userCollection;
    private User userData;

    private NotificationHandler mNotificationHandler;

    private TextView postUsername;
    private EditText postTweet;
    private EditText editUsername;

    boolean editBool;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        getSupportActionBar().setTitle("Profile");
        setContentView(R.layout.activity_profile_page);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
        }

        mRecyclerView = findViewById(R.id.recycleViewTweets);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mItemList = new ArrayList<>();

        editBool = false;

        mFirestore = FirebaseFirestore.getInstance();
        tweetsCollection = mFirestore.collection("Tweets");
        userCollection = mFirestore.collection("Users");

        postUsername = findViewById(R.id.TweetPostUsername);
        editUsername = findViewById(R.id.TweetPostUsernameEdit);
        userCollection.document(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    userData = new User(document.getData().get("id").toString(), document.getData().get("username").toString(), document.getData().get("phone").toString(), document.getData().get("gender").toString());
                    postUsername.setText(userData.getUsername());
                    mAdapter = new TweetCardAdapter(this, mItemList, user, userData);
                    mRecyclerView.setAdapter(mAdapter);
                    queryData();
                }
            }
        });

        postTweet = findViewById(R.id.TweetPostTweet);

        mNotificationHandler = new NotificationHandler(this);

    }

    private void queryData() {

        mItemList.clear();

        tweetsCollection.whereEqualTo("uid", user.getUid()).orderBy("currentTime", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                TweetCard item = doc.toObject(TweetCard.class);
                item.setId(doc.getId());
                mItemList.add(item);
            }

            mAdapter.notifyDataSetChanged();
        });

    }

    public void deleteTweet(TweetCard tweet) {
        DocumentReference ref = tweetsCollection.document(tweet._getId());
        ref.delete().addOnSuccessListener(success -> {
        }).addOnFailureListener(failure -> {
            Toast.makeText(this, "Delete Failed!", Toast.LENGTH_LONG).show();
        });
        queryData();
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.top_menu_home:
                finish();
                intent = new Intent(this, HomePageActivity.class);
                startActivity(intent);
                return true;
            case R.id.top_menu_profile:
                finish();
                intent = new Intent(this, ProfilePageActivity.class);
                startActivity(intent);
                return true;
            case R.id.top_menu_logout:
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
            tweetsCollection.add(new TweetCard(user.getUid(), postUsername.getText().toString(), postTweet.getText().toString(), 2131230854, new Timestamp(System.currentTimeMillis())));
            mNotificationHandler.send("Tweet sent!", postUsername.getText().toString() + " posted: " + postTweet.getText().toString());
            postTweet.setText("");
            queryData();

        }
    }


    public void edit(View view) {
        if (editBool) {
            editUsername.setVisibility(View.INVISIBLE);
            postUsername.setVisibility(View.VISIBLE);
        } else {
            editUsername.setText(postUsername.getText());
            editUsername.setVisibility(View.VISIBLE);
            postUsername.setVisibility(View.INVISIBLE);
        }


        if (editBool && !editUsername.getText().toString().equals(postUsername.getText().toString())) {
            Log.i(LOG_TAG, "changed from: " + postUsername.getText() + " to: " + editUsername.getText());
            userData.setUsername(editUsername.getText().toString());
            userCollection.document(user.getUid()).set(userData);
            postUsername.setText(userData.getUsername());

            tweetsCollection.orderBy("currentTime", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    TweetCard item = doc.toObject(TweetCard.class);
                    item.setId(doc.getId());
                    if (item.getUid().equals(user.getUid())) {
                        item.setUsername(userData.getUsername());
                        tweetsCollection.document(doc.getId()).set(item);
                    }
                }
                queryData();
            });
        }
        editBool = !editBool;
    }
}