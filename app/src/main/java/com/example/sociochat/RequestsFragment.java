package com.example.sociochat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment
{

    private View RequestFragementView;
    private RecyclerView myRequestList;

    private DatabaseReference ChatRequestRef,UserRef,ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private TextView norequest;



    public RequestsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragementView =  inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
            norequest = RequestFragementView.findViewById(R.id.norequests);


        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRequestList = (RecyclerView) RequestFragementView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));



        return RequestFragementView;

    }


    @Override
    public void onStart()
    {
        super.onStart();
        norequest.setVisibility(View.VISIBLE);



        FirebaseRecyclerOptions<Contacts> options
                = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentUserID),Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model)
                    {



                         final String list_user_id = getRef(position).getKey();

                         DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                         getTypeRef.addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                             {
                                  if(dataSnapshot.exists())
                                  {
                                      String type = dataSnapshot.getValue().toString();

                                              if(type.equals("received"))
                                              {
                                                  UserRef.child(list_user_id).addValueEventListener(new ValueEventListener()
                                                  {
                                                      @Override
                                                      public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                                      {
                                                            if(dataSnapshot.hasChild("image"))
                                                            {

                                                                final String requestProfileImage = dataSnapshot.child("image").getValue().toString();


                                                                Picasso.get().load(requestProfileImage).into(holder.profileImage);

                                                            }
                                                            final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                            final String requestUserStatus = dataSnapshot.child("Status").getValue().toString();

                                                          norequest.setVisibility(View.GONE);
                                                            holder.userName.setText(requestUserName);
                                                            holder.userName.setVisibility(View.VISIBLE);
                                                            holder.userStatus.setText("wants to Connect with you");
                                                            holder.userStatus.setVisibility(View.VISIBLE);
                                                          holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                                                          holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);




                                                          holder.itemView.setOnClickListener(new View.OnClickListener()
                                                            {
                                                                @Override
                                                                public void onClick(View v)
                                                                {
                                                                    CharSequence options[] = new CharSequence[]
                                                                            {
                                                                                    "Accept",
                                                                                    "Cancel"
                                                                            };

                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                                    builder.setTitle(requestUserName + "Chat Request");

                                                                    builder.setItems(options, new DialogInterface.OnClickListener()
                                                                    {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int i)
                                                                        {
                                                                             if(i == 0)
                                                                             {
                                                                                  ContactsRef.child(currentUserID).child(list_user_id).child("Contacts")
                                                                                  .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                  {
                                                                                      @Override
                                                                                      public void onComplete(@NonNull Task<Void> task)
                                                                                      {
                                                                                          if(task.isSuccessful())
                                                                                          {
                                                                                              ContactsRef.child(list_user_id).child(currentUserID).child("Contacts")
                                                                                                      .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                              {
                                                                                                  @Override
                                                                                                  public void onComplete(@NonNull Task<Void> task)
                                                                                                  {
                                                                                                      if(task.isSuccessful())
                                                                                                      {
                                                                                                          ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                                                                  .removeValue()
                                                                                                                  .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                                  {
                                                                                                                      @Override
                                                                                                                      public void onComplete(@NonNull Task<Void> task)
                                                                                                                      {
                                                                                                                          if(task.isSuccessful())
                                                                                                                          {
                                                                                                                              ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                                                      .removeValue()
                                                                                                                                      .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                                                      {
                                                                                                                                          @Override
                                                                                                                                          public void onComplete(@NonNull Task<Void> task)
                                                                                                                                          {
                                                                                                                                              if(task.isSuccessful())
                                                                                                                                              {
                                                                                                                                                  Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();

                                                                                                                                              }

                                                                                                                                          }
                                                                                                                                      });

                                                                                                                          }

                                                                                                                      }
                                                                                                                  });

                                                                                                      }

                                                                                                  }
                                                                                              });

                                                                                          }

                                                                                      }
                                                                                  });
                                                                             }
                                                                             if(i == 1)
                                                                             {
                                                                                 ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                                         .removeValue()
                                                                                         .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                         {
                                                                                             @Override
                                                                                             public void onComplete(@NonNull Task<Void> task)
                                                                                             {
                                                                                                 if(task.isSuccessful())
                                                                                                 {
                                                                                                     ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                             .removeValue()
                                                                                                             .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                             {
                                                                                                                 @Override
                                                                                                                 public void onComplete(@NonNull Task<Void> task)
                                                                                                                 {
                                                                                                                     if(task.isSuccessful())
                                                                                                                     {
                                                                                                                         Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();

                                                                                                                     }

                                                                                                                 }
                                                                                                             });

                                                                                                 }

                                                                                             }
                                                                                         });

                                                                             }
                                                                        }
                                                                    });

                                                                 builder.show();

                                                                }
                                                            });

                                                      }

                                                      @Override
                                                      public void onCancelled(@NonNull DatabaseError databaseError)
                                                      {

                                                      }
                                                  });
                                              }
                                  }
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError databaseError)
                             {

                             }
                         });




                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);

                        RequestViewHolder holder = new RequestViewHolder(view);
                        return holder;
                    }
                };

        myRequestList.setAdapter(adapter);
        adapter.startListening();


    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName,userStatus;
        CircleImageView profileImage;
        Button AcceptButton,CancelButton;




        public RequestViewHolder(@NonNull View itemView)
        {
            super(itemView);


            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_btn);
            CancelButton = itemView.findViewById(R.id.request_cancel_btn);

            userName.setVisibility(View.GONE);
            userStatus.setVisibility(View.GONE);
            profileImage.setVisibility(View.GONE);
            AcceptButton.setVisibility(View.GONE);
            CancelButton.setVisibility(View.GONE);

        }
    }

}
