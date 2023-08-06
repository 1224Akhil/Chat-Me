package com.example.chattingapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.chattingapp.Network.ApiClient;
import com.example.chattingapp.Network.ApiService;
import com.example.chattingapp.R;
import com.example.chattingapp.adapters.ChatAdapter;
import com.example.chattingapp.databinding.ActivityChatBinding;
import com.example.chattingapp.models.ChatMessage;
import com.example.chattingapp.models.User;
import com.example.chattingapp.utilities.Constants;
import com.example.chattingapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.mlkit.nl.smartreply.SmartReply;
import com.google.mlkit.nl.smartreply.SmartReplyGenerator;
import com.google.mlkit.nl.smartreply.SmartReplySuggestion;
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult;
import com.google.mlkit.nl.smartreply.TextMessage;
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User recieverUser;

    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;

    private Boolean isRecievedAvailable = false;

    private List<TextMessage> conversation;



    SmartReplyGenerator smartReply;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        conversation = new ArrayList<>();
        blockScreenShot();
        setListners();
        loadReceiverDetails();
        init();
        listenMessage();
    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages,getBitmapFromEncodedStream(recieverUser.image),preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();

    }


    private void blockScreenShot(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
    }
    private void sendMessage(){
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,recieverUser.id);
        message.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId != null){
            updateConversion(binding.inputMessage.getText().toString());
        }else{
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_NAME,recieverUser.name);
            conversion.put(Constants.KEY_RECEIVER_ID,recieverUser.id);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,recieverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);

        }

        if(!isRecievedAvailable){
            try{
                JSONArray token = new JSONArray();
                token.put(recieverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME,preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_data,data);
                body.put(Constants.REMOTE_MSG_Registrations_IDS,token);

                sendNotification(body.toString());

            }catch (Exception exception){
                showToast(exception.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMessage(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if(response.isSuccessful()){
                    try {
                        if(response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return ;
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfully");
                }else{
                    showToast("error = "+response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void listnerAvailabilityOfReciever(){
        database.collection(Constants.KEY_COLLECTIONS_USERS).document(recieverUser.id).addSnapshotListener(ChatActivity.this,(value,error)->
                {
                    if(error != null){
                        return;
                    }
                    if(value != null){
                        if(value.getLong(Constants.KEY_AVAILABILITY)!=null){
                            int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                            isRecievedAvailable = availability == 1;
                        }
                        recieverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                        if(recieverUser.image == null){
                            recieverUser.image = value.getString(Constants.KEY_IMAGE);
                            chatAdapter.setReceiverProfileImage(getBitmapFromEncodedStream(recieverUser.image));
                            chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
                        }
                    }

                    if(isRecievedAvailable){
                        binding.textAvailability.setVisibility(View.VISIBLE);
                    }else{
                        binding.textAvailability.setVisibility(View.GONE);
                    }

                });

    }


    private void listenMessage(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,recieverUser.id)
                .addSnapshotListener(eventListner);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,recieverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListner);
    }

    private final EventListener<QuerySnapshot> eventListner = (value, error) -> {
        if(error != null){
            return;
        }
        if(value !=null){
            int count = chatMessages.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){

                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateAndTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);

                    if(documentChange.getDocument().getString(Constants.KEY_MESSAGE) != null &&
                            Objects.equals(preferenceManager.getString(Constants.KEY_USER_ID), documentChange.getDocument().getString(Constants.KEY_SENDER_ID))){
                        String k = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                        conversation.add(TextMessage.createForLocalUser(k, System.currentTimeMillis()));
                    } else if (documentChange.getDocument().getString(Constants.KEY_MESSAGE) != null &&
                            Objects.equals(recieverUser.id, documentChange.getDocument().getString(Constants.KEY_SENDER_ID))) {
                        String k = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                        conversation.add(TextMessage.createForRemoteUser(k,
                                System.currentTimeMillis(),recieverUser.id));
                    }

                }
            }
            Collections.sort(chatMessages,(obj1,obj2)->obj1.dateObject.compareTo(obj2.dateObject));
            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);

        if(conversionId == null){
            checkForConversion();

        }
    };
    private Bitmap getBitmapFromEncodedStream(String encodedImage){
        if(encodedImage!=null){
            byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }else{
            return null;
        }
    }

    private void loadReceiverDetails(){
        recieverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(recieverUser.name);
    }

    private void setListners(){
        binding.imageBack.setOnClickListener(v->onBackPressed());
        binding.layoutSend.setOnClickListener(v-> {
            sendMessage();
        });
        binding.inputMessage.setOnClickListener(v->{
            binding.cardView5.setVisibility(View.VISIBLE);
            binding.smartReplyLayout.setVisibility(View.VISIBLE);
            suggestReply();
        });

        binding.textView1.setOnClickListener(v->{
            String val = binding.textView1.getText().toString();
            binding.inputMessage.setText(val);
            binding.smartReplyLayout.setVisibility(View.INVISIBLE);
            binding.cardView5.setVisibility(View.INVISIBLE);
        });

        binding.textView2.setOnClickListener(v->{
            String val = binding.textView2.getText().toString();
            binding.inputMessage.setText(val);
            binding.smartReplyLayout.setVisibility(View.INVISIBLE);
            binding.cardView5.setVisibility(View.INVISIBLE);

        });
        binding.textView3.setOnClickListener(v->{
            String val = binding.textView3.getText().toString();
            binding.inputMessage.setText(val);
            binding.smartReplyLayout.setVisibility(View.INVISIBLE);
            binding.cardView5.setVisibility(View.INVISIBLE);
        });

    }
    private String getReadableDateAndTime(Date date){
        return new SimpleDateFormat("MMMM dd,yyyy-hh:mm a",Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String,Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE,message,
                Constants.KEY_TIMESTAMP,new Date()
        );
    }
    private void checkForConversion(){
        if(chatMessages.size()!=0){
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    recieverUser.id
            );
            checkForConversionRemotely(
                    recieverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }
    private void checkForConversionRemotely(String senderId,String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversioOnCompleteListner);
    }

    private final OnCompleteListener<QuerySnapshot> conversioOnCompleteListner = task->{
      if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
          DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
          conversionId = documentSnapshot.getId();
      }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listnerAvailabilityOfReciever();
    }

    private void suggestReply(){
        if(conversation.size() > 0){
            smartReply = SmartReply.getClient();
            smartReply.suggestReplies(conversation)
                    .addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            binding.textView1.setText("");
                            binding.textView2.setText("");
                            binding.textView3.setText("");

                            SmartReplySuggestionResult result = (SmartReplySuggestionResult) o;
                            if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                                // The conversation's language isn't supported, so
                                // the result doesn't contain any suggestions.
                            } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                                // Task completed successfully
                                // ...
                                ArrayList<String> res = new ArrayList<>();
                                for (SmartReplySuggestion suggestion : result.getSuggestions()) {
                                    String replyText = suggestion.getText();
                                    res.add(replyText);
                                }
                                binding.textView1.setText(res.get(0));
                                binding.textView2.setText(res.get(1));
                                binding.textView3.setText(res.get(2));

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    });
        }

    }


}