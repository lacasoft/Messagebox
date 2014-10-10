package com.globant.message.box.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.widget.Button;
import android.widget.EditText;

import com.globant.message.box.MessageboxSingleton;
import com.globant.message.box.R;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBEntityCallback;
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

        passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                enableSubmitIfReady();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        smackAndroid = SmackAndroid.init(this);

        if (!QBChatService.isInitialized()) {
            QBChatService.init(this);
        }

        chatService = QBChatService.getInstance();
    }

    public void enableSubmitIfReady() {

        boolean isReady = passwordEdit.getText().toString().length()>7;

        if (isReady) {
            registerButton.setEnabled(true);
        } else {
            registerButton.setEnabled(false);
        }
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

        //QBUsers.signUpSignInTask(user, (QBEntityCallback<QBUser>) this);
        registerToChat(user);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
    }

    private void registerToChat(final QBUser user)
    {
        // register user
        QBUsers.signUpSignInTask(user, new QBEntityCallback() {
            @Override
            public void onSuccess(Object o, Bundle bundle) {
                loginToChat(user);
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(List list) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(RegistrationActivity.this);
                dialog.setMessage(getApplicationContext().getResources().getString(R.string.alert_error_login) + list).create().show();

                progressDialog.dismiss();
            }
        });
    }

    private void loginToChat(final QBUser user){
        ((MessageboxSingleton) getApplication()).setCurrentUser(user);

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        Log.i(TAG, "success when login");
        Intent i = new Intent();
        setResult(RESULT_OK, i);

        //
        Intent intent = new Intent(RegistrationActivity.this, DialogsActivity.class);
        startActivity(intent);
        finish();
    }

}
