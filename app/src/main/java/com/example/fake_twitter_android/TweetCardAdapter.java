package com.example.fake_twitter_android;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TweetCardAdapter extends RecyclerView.Adapter<TweetCardAdapter.ViewHolder> implements Filterable {

    Timestamp now = new Timestamp(System.currentTimeMillis());
    private static final String LOG_TAG = TweetCardAdapter.class.getName();

    private FirebaseUser user;
    private User userData;

    private ArrayList<TweetCard> mTweetCardListData;
    private ArrayList<TweetCard> mTweetCardListDataAll;
    Context mContext;
    private int lastPosition = -1;


    public TweetCardAdapter(Context context, ArrayList<TweetCard> itemsData,FirebaseUser userin, User userData) {
        this.mTweetCardListData = itemsData;
        this.mTweetCardListDataAll = itemsData;
        this.mContext = context;
        this.user=userin;
        this.userData=userData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.tweet_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TweetCardAdapter.ViewHolder holder, int position) {
        TweetCard currentTweet = mTweetCardListData.get(position);

        holder.bindTo(currentTweet);

        if (holder.getAdapterPosition() > lastPosition) {
            Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(anim);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mTweetCardListData.size();
    }

    @Override
    public Filter getFilter() {
        return tweetFilter;
    }

    private Filter tweetFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<TweetCard> filteredTweets = new ArrayList<>();
            FilterResults res = new FilterResults();

            if (charSequence == null || charSequence.length() == 0) {
                res.count = mTweetCardListData.size();
                res.values = mTweetCardListDataAll;
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (TweetCard card : mTweetCardListDataAll) {
                    if (card.getUsername().toLowerCase().contains(filterPattern)) {
                        filteredTweets.add(card);
                    }
                }
                res.count = filteredTweets.size();
                res.values = filteredTweets;
            }

            return res;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mTweetCardListData = (ArrayList) filterResults.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView username;
        private TextView date;
        private TextView tweet;
        private ImageView pfp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.TweetCardUsername);
            date = itemView.findViewById(R.id.TweetCardDate);
            tweet = itemView.findViewById(R.id.TweetCardTweet);
            pfp = itemView.findViewById(R.id.UserPfp);

        }

        public void bindTo(TweetCard currentTweet) {

            int seconds = ((int) (now.getTime() - currentTweet.getCurrentTime().getTime())) / 1000;
            if (seconds<0){
                seconds*=-1;
            }

            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            seconds = (seconds % 3600) % 60;

            String pattern = "yyyy.MM.d HH:mm";
            SimpleDateFormat simpleDateFormat =new SimpleDateFormat(pattern, new Locale("en", "EN"));

            date.setText(String.valueOf(seconds)+"s");
            if (minutes>=1){
                date.setText(String.valueOf(minutes)+"m");
            }
            if (hours>=1){
                date.setText(String.valueOf(hours)+"h");
            }
            if (hours>72){
                date.setText(simpleDateFormat.format(currentTweet.getCurrentTime()));
            }

            username.setText(currentTweet.getUsername());
            tweet.setText(currentTweet.getTweet());
            //Glide.with(mContext).load(currentTweet.getPfp()).into(pfp);

            if (mContext.getClass()==HomePageActivity.class){
                itemView.findViewById(R.id.twitterDeleteTweet).setOnClickListener(view -> ((HomePageActivity)mContext).deleteTweet(currentTweet));
                itemView.findViewById(R.id.twitterDeleteTweet).setVisibility(View.INVISIBLE);
                if (currentTweet.getUid().equals(userData.getId())){
                    itemView.findViewById(R.id.twitterDeleteTweet).setVisibility(View.VISIBLE);
                }
            }
            if (mContext.getClass()==ProfilePageActivity.class){
                itemView.findViewById(R.id.twitterDeleteTweet).setOnClickListener(view -> ((ProfilePageActivity)mContext).deleteTweet(currentTweet));
                itemView.findViewById(R.id.twitterDeleteTweet).setVisibility(View.INVISIBLE);
                if (currentTweet.getUid().equals(userData.getId())){
                    itemView.findViewById(R.id.twitterDeleteTweet).setVisibility(View.VISIBLE);
                }
            }



        }
    }
}


