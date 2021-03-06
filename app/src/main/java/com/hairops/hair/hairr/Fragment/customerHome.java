package com.hairops.hair.hairr.Fragment;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hairops.hair.hairr.Model.Post;
import com.hairops.hair.hairr.R;
import com.hairops.hair.hairr.Utils.CircleTransform;
import com.hairops.hair.hairr.imageviewer;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * A simple {@link Fragment} subclass.
 */
public class customerHome extends Fragment {

    FirebaseAuth auth;
    private RecyclerView postList;
    private DatabaseReference mUsersDatabase, postDatabase,likes;
    private LinearLayoutManager mLayoutManager;
    private LinearLayout comment;
    private String key,uid;
    imageviewer imageviewer;
    private boolean mProcessLike = false;


    public customerHome() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_customer_home, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        postDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");
        likes = FirebaseDatabase.getInstance().getReference().child("Likes");
        postList = (RecyclerView) view.findViewById(R.id.lastestNews);

        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setReverseLayout(true);
        postDatabase.keepSynced(true);
        likes.keepSynced(true);
        postList.setHasFixedSize(true);
        postList.setLayoutManager(mLayoutManager);
       imageviewer = new imageviewer(getContext());

    }



    @Override
    public void onResume() {
        super.onResume();
        initAdapter();
    }

    @Override
    public void onStart() {
        super.onStart();
        initAdapter();
    }

    private void initAdapter() {
        FirebaseRecyclerAdapter<Post, PostViewHolder> adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(
                Post.class,
                R.layout.singlepostitem,
                PostViewHolder.class,
                postDatabase
        ) {
            @Override
            protected void populateViewHolder(PostViewHolder viewHolder, Post model, int position) {
                key = getRef(position).getKey();
                String uid = auth.getCurrentUser().getUid();

                viewHolder.setDisplayName(model.getUsername());
                viewHolder.setPostImage(model.getPostImageUrl(), getContext());
                viewHolder.setUserImage(model.getUserImage(), getContext());
                viewHolder.setLikeBtn(key);
                viewHolder.setNumberOfLikes(key);
                viewHolder.postText.setText(model.getPosttText());
                viewHolder.date.setText(model.getDate());
                viewHolder.userLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //ToDO show dialog to see user profile
                        final Dialog dialog = new Dialog(getContext());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialoguserprofile);
                        final ImageView stylistImage = dialog.findViewById(R.id.userImageThumbNail);
                        TextView styistName = dialog.findViewById(R.id.stylistName);
                        TextView stylistSpacialization = dialog.findViewById(R.id.stylistName);
                        FancyButton button = dialog.findViewById(R.id.close);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });

                        Picasso.with(getContext()).load(model.getUserImage()).transform(new CircleTransform()).into(stylistImage);

                        styistName.setText(model.getUsername());
                        stylistSpacialization.setText(model.getUserSpecialization());


                        dialog.show();
                    }
                });
                viewHolder.like.setOnClickListener(view -> {

                    mProcessLike = true;

                        likes.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (mProcessLike) {

                                    if (dataSnapshot.child(key).hasChild(uid)) {
                                        likes.child(key).child(uid).removeValue();
                                        viewHolder.like.setImageResource(R.drawable.ic_unlike);
                                        mProcessLike = false;
                                        viewHolder.setLikeBtn(key);
                                        viewHolder.setNumberOfLikes(key);

                                    } else {
                                        likes.child(key).child(uid).setValue("liked");
                                        viewHolder.like.setImageResource(R.drawable.ic_like);
                                        mProcessLike = false;
                                        viewHolder.setLikeBtn(key);
                                        viewHolder.setNumberOfLikes(key);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                });
                viewHolder.comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), com.hairops.hair.hairr.comment.class);
                        intent.putExtra("postid", model.getPosterId());
                        startActivity(intent);




                    }
                });
                viewHolder.postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imageviewer.startdi(model.getPostImageUrl(),model.getPosttText());

                    }
                });


            }
        };

        postList.setAdapter(adapter);
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView comment;
        ImageView like;
        ImageView userImage;
        ImageView postImage;
        LinearLayout userLayout;
        FirebaseAuth auth;
        TextView numberOfLikes,postText, date;
        DatabaseReference likes;
        String uidd;


        public PostViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            like = (ImageView) mView.findViewById(R.id.postLike);
            comment = (ImageView) mView.findViewById(R.id.postComment);
            userImage = (ImageView) mView.findViewById(R.id.posterImage);
            postImage = (ImageView) mView.findViewById(R.id.postImage);
            userLayout = (LinearLayout) mView.findViewById(R.id.linUser);
            numberOfLikes = (TextView)mView.findViewById(R.id.numberOfLikes);
            postText = (TextView)mView.findViewById(R.id.postalText);
            date = mView.findViewById(R.id.date);
            auth = FirebaseAuth.getInstance();
            uidd = auth.getCurrentUser().getUid();
            likes = FirebaseDatabase.getInstance().getReference().child("Likes");
            likes.keepSynced(true);

        }

        public void setNumberOfLikes(String key){

            likes.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int size = (int) dataSnapshot.getChildrenCount();

                    numberOfLikes.setText(String.valueOf(size));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }

        public void setLikeBtn(String key){

//            likes.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.exists()){
//
//                    }
//                    if (dataSnapshot.child(key).hasChild(uidd)){
//                        like.setImageResource(R.drawable.ic_like);
//
//                    }else {
//                        like.setImageResource(R.drawable.ic_unlike);
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });

            likes.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {


                        if (dataSnapshot.child(key).hasChild(uidd)) {
                            like.setImageResource(R.drawable.ic_like);

                        } else {
                            like.setImageResource(R.drawable.ic_unlike);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        public void setDisplayName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.posterName);
            userNameView.setText(name);

        }

        public void setUserImage(String status, Context context) {

            Picasso.with(context).load(status).transform(new CircleTransform()).networkPolicy(NetworkPolicy.OFFLINE).into(userImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context)
                            .load(status).into(userImage);

                }
            });


        }


        public void setPostImage(String status, Context context) {

            Picasso.with(context).load(status).networkPolicy(NetworkPolicy.OFFLINE).into(postImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context)
                            .load(status).into(postImage);

                }
            });


        }


    }

}


