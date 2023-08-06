package com.example.chattingapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.example.chattingapp.R;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.util.Collections;

public class Call extends AppCompatActivity {


    EditText editText;
    TextView textView,textView1;
    ZegoSendCallInvitationButton voiceCallBtn,videoCallbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);


        editText = findViewById(R.id.editText);
        voiceCallBtn = findViewById(R.id.voiceCall);
        videoCallbtn = findViewById(R.id.videoCall);
        textView = findViewById(R.id.textView);
        textView1 =findViewById(R.id.textView2);

        String userID = getIntent().getStringExtra("userName");

        textView1.setText(userID);



        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String tragetUserID = editText.getText().toString().trim();
                setVideoCall(tragetUserID);
                setVoiceCall(tragetUserID);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public void setVoiceCall(String targetUserID){
        voiceCallBtn.setIsVideoCall(false);
        voiceCallBtn.setResourceID("zego_uikit_call");
        voiceCallBtn.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetUserID,targetUserID)));
    }
    public void setVideoCall(String targetUserID){
        videoCallbtn.setIsVideoCall(true);
        videoCallbtn.setResourceID("zego_uikit_call");
        videoCallbtn.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetUserID,targetUserID)));
    }
}