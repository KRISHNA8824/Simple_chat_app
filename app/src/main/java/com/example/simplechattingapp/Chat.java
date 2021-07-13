package com.example.simplechattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends AppCompatActivity {
    String  ReceiverName,ReceiverImage, ReceiverUid, SenderUid;
    CircleImageView profileImage;
    TextView receiverName;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth mAuth;
    public  static String sImage;
    public  static String rImage;

    EditText editMessage;
    CardView send_btn;

    String SenderRoom, ReceiverRoom;
    RecyclerView messageAdapter;

    ArrayList<Messages> messagesArrayList;

    MessagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ReceiverName = getIntent().getStringExtra("Name");
        ReceiverImage = getIntent().getStringExtra("ReceiverImage");
        ReceiverUid = getIntent().getStringExtra("Uid");


        messagesArrayList = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        SenderUid =mAuth.getUid();

        SenderRoom = SenderUid + ReceiverUid;
        ReceiverRoom = ReceiverUid + SenderUid;

        messageAdapter = findViewById(R.id.messageAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageAdapter.setLayoutManager(linearLayoutManager);
        adapter = new MessagesAdapter(Chat.this, messagesArrayList);
        messageAdapter.setAdapter(adapter);

        profileImage = findViewById(R.id.profile_image);
        Picasso.get().load(ReceiverImage).into(profileImage);

        receiverName = findViewById(R.id.receiverName);
        receiverName.setText(""+ReceiverName);

        editMessage = findViewById(R.id.edit_message);
        send_btn = findViewById(R.id.send_btn);


        DatabaseReference databaseReference = firebaseDatabase.getReference().child("user").child(mAuth.getUid());
        DatabaseReference chatReference = firebaseDatabase.getReference().child("chats").child(SenderRoom).child("Messages");


        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    messagesArrayList.add(messages);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               sImage = snapshot.child("imageUri").getValue().toString();
               rImage = ReceiverImage;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editMessage.getText().toString();
                if(message.isEmpty()){
                    Toast.makeText(Chat.this, "Please Enter valid Message", Toast.LENGTH_SHORT).show();
                    return;
                }
                editMessage.setText("");
                Date date = new Date();

                Messages messages = new Messages(message, SenderUid, date.getTime());
                firebaseDatabase = FirebaseDatabase.getInstance();
                firebaseDatabase.getReference().child("chats").child(SenderRoom).child("Messages").push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        firebaseDatabase.getReference().child("chats").child(ReceiverRoom).child("Messages").push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }
                });
            }
        });


    }
}