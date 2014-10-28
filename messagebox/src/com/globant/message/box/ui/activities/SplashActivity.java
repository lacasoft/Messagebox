package com.globant.message.box.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.model.QBSession;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.users.model.QBUser;
import com.globant.message.box.MessageboxSingleton;
import com.globant.message.box.R;

import org.jivesoftware.smack.SmackException;

import java.util.List;

public class SplashActivity extends Activity {

    /**
     * QuickBlox Aplication Info
     */
    private static String APP_ID = "";
    private static String AUTH_KEY = "";
    private static String AUTH_SECRET = "";

    /**
     *  User Owner Api
     */
    private static String USER_LOGIN = "";
    private static String USER_PASSWORD = "";

    static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 60;

    private QBChatService chatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        this.APP_ID = getApplicationContext().getResources().getString(R.string.quick_id);
        this.AUTH_KEY = getApplicationContext().getResources().getString(R.string.quick_auth_key);
        this.AUTH_SECRET = getApplicationContext().getResources().getString(R.string.quick_auth_secret);

        this.USER_LOGIN = getApplicationContext().getResources().getString(R.string.quick_user_name); //"tester"; //
        this.USER_PASSWORD = getApplicationContext().getResources().getString(R.string.quick_user_pass); //"12345678"; //
        // Init Chat
        //
        QBChatService.setDebugEnabled(true);

        QBSettings.getInstance().fastConfigInit(this.APP_ID, this.AUTH_KEY, this.AUTH_SECRET);

        if (!QBChatService.isInitialized()) {
            QBChatService.init(this);
        }

        chatService = QBChatService.getInstance();

        // create QB user
        //
        final QBUser user = new QBUser();
        user.setLogin(this.USER_LOGIN);
        user.setPassword(this.USER_PASSWORD);

        QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>(){
            @Override
            public void onSuccess(QBSession session, Bundle args) {

                // save current user
                //
                user.setId(session.getUserId());
                ((MessageboxSingleton)getApplication()).setCurrentUser(user);

                // login to Chat
                //
                loginToChat(user);
            }

            @Override
            public void onError(List<String> errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SplashActivity.this);
                dialog.setMessage(getApplicationContext().getResources().getString(R.string.alert_error_session) + errors).create().show();
            }
        });
    }

    private void loginToChat(final QBUser user){

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
                Intent intent = new Intent(SplashActivity.this, DialogsActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(List errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SplashActivity.this);
                dialog.setMessage(getApplicationContext().getResources().getString(R.string.alert_error_login) + errors).create().show();
            }
        });
    }
}