package com.example.sociochat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.icu.text.RelativeDateTimeFormatter;
import android.net.Uri;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.Typeface.BOLD;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
   // private static final int SPAN_INCLUSIVE_INCLUSIVE =0 ;
    private List<com.example.sociochat.Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter(List<com.example.sociochat.Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {

        public TextView senderMessageText,receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture,messageReceiverPicture;



        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage =  (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_message_layout,viewGroup,false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }





    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position)
    {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);


        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });




        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);





        if(fromMessageType.equals("text"))
        {




            if(fromUserID.equals(messageSenderID))
            {
                int span2length = (messages.getTime().length())+2;
                SpannableString spannable1 = new SpannableString(messages.getMessage());
                spannable1.setSpan(new ForegroundColorSpan(Color.WHITE), 0, messages.getMessage().length(),0);
                spannable1.setSpan(new StyleSpan(Typeface.BOLD), 0, messages.getMessage().length(), 0);
                SpannableString spannable2 = new SpannableString("\n\n"+messages.getTime());
                spannable2.setSpan(new RelativeSizeSpan(0.5f),  0,span2length,0);
                spannable2.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),0,span2length,0);

                /*SpannableString span1 = new SpannableString(messages.getMessage());
                span1.setSpan(new ForegroundColorSpan(Color.WHITE),0,messages.getMessage().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                //span1.setSpan(new ForegroundColorSpan(Color.WHITE),0,span1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                span1.setSpan(new StyleSpan(BOLD),0,messages.getMessage().length(),Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                String messagetime =("\n"+messages.getTime()) ;
                SpannableString span2 = new SpannableString(messagetime);
                span1.setSpan(new ForegroundColorSpan(Color.GRAY),0,messagetime.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                span2.setSpan(new AbsoluteSizeSpan(9,true), 0, messagetime.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                //String finalspan2 = ("<p style=\"color:#808080\";>"+span2+"</p>");*/
               CharSequence finalmessage = TextUtils.concat(spannable1,"",spannable2);


                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_lyaout);
                //messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                messageViewHolder.senderMessageText.setText(finalmessage);//messages.getMessage() + "\n \n" + messages.getTime() + "-" + messages.getDate());

            }
            else
            {
                int span1length = (messages.getMessage().length())+1;
                int span2length = (messages.getTime().length())+4;
                SpannableString spannable1 = new SpannableString(" "+messages.getMessage());
                spannable1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, span1length,0);
                spannable1.setSpan(new StyleSpan(Typeface.BOLD), 0,span1length, 0);
                SpannableString spannable2 = new SpannableString("  \n\n"+messages.getTime());
                spannable2.setSpan(new RelativeSizeSpan(0.5f),  0,span2length,0);
                spannable2.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),0,span2length,0);

                CharSequence finalmessage = TextUtils.concat(spannable1,"",spannable2);



                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
               // messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(finalmessage);

            }
        }
        else  if(fromMessageType.equals("image"))
        {
            if (fromUserID.equals((messageSenderID)))
            {
                messageViewHolder.messageSenderPicture.setVisibility((View.VISIBLE));

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);

                messageViewHolder.messageSenderPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                        intent.putExtra("url",userMessagesList.get(position).getMessage());
                        messageViewHolder.itemView.getContext().startActivity(intent);

                    }
                });


            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility((View.VISIBLE));
                messageViewHolder.messageReceiverPicture.setVisibility((View.VISIBLE));
                messageViewHolder.messageReceiverPicture.setBackgroundResource(R.drawable.imagesbackground);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);

            }
        }
        else  if(fromMessageType.equals("docx") || fromMessageType.equals("pdf"))
        {
            if (fromUserID.equals((messageSenderID)))
            {
                messageViewHolder.messageSenderPicture.setVisibility((View.VISIBLE));

                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/whatsapp-edd5a.appspot.com/o/Image%20Files%2Ffile.xml?alt=media&token=5a919e8a-cf08-4355-9549-e994b5743686")
                        .into(messageViewHolder.messageSenderPicture);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }
            else
            {

                messageViewHolder.receiverProfileImage.setVisibility((View.VISIBLE));
                messageViewHolder.messageReceiverPicture.setVisibility((View.VISIBLE));

                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/whatsapp-edd5a.appspot.com/o/Image%20Files%2Ffile.xml?alt=media&token=5a919e8a-cf08-4355-9549-e994b5743686")
                        .into(messageViewHolder.messageReceiverPicture);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }

        }

        if (fromUserID.equals(messageSenderID))
        {
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download and View This file",
                                        "Delete For me",
                                        "Delete For Everyone",
                                        "Cancel",

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select Option");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (which == 0)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                                else if (which == 1)
                                {
                                    deletesentMessage(position,messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 2)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Delete For Everyone",
                                        "Cancel",

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select Option");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (which == 0)
                                {
                                    deletesentMessage(position,messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 1)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }


                            }
                        });
                        builder.show();
                    }
                    if (userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {


                                        "Delete For me",
                                        "Delete For Everyone",
                                        "Cancel",

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select Option");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                                if (which == 0)
                                {
                                    deletesentMessage(position,messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);


                                }
                                else if (which == 1)
                                {
                                    deleteMessageForEveryone(position,messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }


                            }
                        });
                        builder.show();
                    }
                    return true;
                }
            });
        }
        else
        {
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download and View This file",
                                        "Delete For me",
                                        "Cancel",

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select Option");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (which == 0)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                                else if (which == 1)
                                {
                                    deleteReceiveMessage(position,messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 2)
                                {

                                }

                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Cancel",

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select Option");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (which == 0)
                                {
                                    deleteReceiveMessage(position,messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }



                            }
                        });
                        builder.show();
                    }
                    if (userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {

                                        "Delete For me",

                                        "Cancel",

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Select Option");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if (which == 0)
                                {
                                    deleteReceiveMessage(position,messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                               /* else if (which == 1)
                                {
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }*/



                            }
                        });
                        builder.show();
                    }
                    return true;
                }
            });
        }


    }




    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

    private void deletesentMessage(final int position , final MessageViewHolder holder)
    {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void deleteReceiveMessage(final int position , final MessageViewHolder holder)
    {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void deleteMessageForEveryone(final int position , final MessageViewHolder holder)
    {

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    rootRef.child("Message")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });


                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }





}
