package com.globant.message.box.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.widget.Button;
import android.widget.EditText;

import com.globant.message.box.MessageboxSingleton;
import com.globant.message.box.R;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;

import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.List;

public class RegistrationActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "RegistrationActivity";

    private Button registerButton;
    private EditText loginEdit;
    private EditText passwordEdit;
    private ProgressDialog progressDialog;

    private String login;
    private String password;
    private QBUser user;
    private SmackAndroid smackAndroid;

    static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    private QBChatService chatService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        chatService = QBChatService.getInstance();

        loginEdit = (EditText) findViewById(R.id.loginEdit);
        passwordEdit = (EditText) findViewById(R.id.passwordEdit);
        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");

        smackAndroid = SmackAndroid.init(this);

        if (!QBChatService.isInitialized()) {
            QBChatService.init(this);
        }

        chatService = QBChatService.getInstance();
    }

    @Override
    protected void onDestroy() {
        smackAndroid.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        login = loginEdit.getText().toString();
        password = passwordEdit.getText().toString();

        user = new QBUser(login, password);

        progressDialog.show();

        loginToChat(user);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
    }

    private void loginToChat(final QBUser user){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatService.login(user, new QBEntityCallbackImpl() {
                    @Override
                    public void onSuccess() {

                        // Start sending presences
                        //
                        try {
                            chatService.startAutoSendPresence(AUTO_PRESENCE_INTERVAL_IN_SECONDS);
                        } catch (SmackException.NotLoggedInException e) {
                            e.printStackTrace();
                        }

                        // go to Dialogs screen
                        //
                        Intent intent = new Intent(RegistrationActivity.this, DialogsActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(List errors) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(RegistrationActivity.this);
                        dialog.setMessage(getApplicationContext().getResources().getString(R.string.alert_error_login) + errors).create().show();
                    }
                });
            }
        });
    }
}
