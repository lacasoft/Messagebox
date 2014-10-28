package com.globant.message.box.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.globant.message.box.MessageboxSingleton;
import com.globant.message.box.R;
import com.globant.message.box.ui.adapters.DialogsAdapter;
import com.globant.message.box.ui.listener.SwipeListener;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class DialogsActivity extends Activity {
    private static final String TAG = DialogsActivity.class.getSimpleName();

    private int total;
    private TextView countTotal;
    private DialogsAdapter adapter;
    private ProgressBar progressBar;
    private RecyclerView dialogsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogs_activity);

        dialogsListView = (RecyclerView) findViewById(R.id.roomsList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        countTotal = (TextView) findViewById(R.id.textViewCountTotal);

        adapter = new DialogsAdapter(new ArrayList<QBDialog>(), DialogsActivity.this);
        dialogsListView.setAdapter(adapter);

        progressBar.setVisibility(View.GONE);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        dialogsListView.setLayoutManager(layoutManager);

        // get dialogs
        //
        QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();

        customObjectRequestBuilder.setPagesLimit(100);

        QBChatService.getChatDialogs(null, customObjectRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBDialog> dialogs, Bundle args) {

                // collect all occupants ids
                //
                List<Integer> usersIDs = new ArrayList<Integer>();

                for(QBDialog dialog : dialogs){
                    usersIDs.addAll(dialog.getOccupants());
                    total += dialog.getUnreadMessageCount();
                }

                countTotal.setText("Total:" + total);

                // Get all occupants info
                //
                QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
                requestBuilder.setPage(1);
                requestBuilder.setPerPage(usersIDs.size());
                //
                QBUsers.getUsersByIDs(usersIDs, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {

                        // Save users
                        //
                        ((MessageboxSingleton)getApplication()).setDialogsUsers(users);

                        // build list view
                        //
                        buildListView(dialogs);
                    }

                    @Override
                    public void onError(List<String> errors) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(DialogsActivity.this);
                        dialog.setMessage(getApplicationContext().getResources().getString(R.string.alert_error_get_occupants) + errors).create().show();
                    }

                });
            }

            @Override
            public void onError(List<String> errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(DialogsActivity.this);
                dialog.setMessage(getApplicationContext().getResources().getString(R.string.alert_error_get_dialogs) + errors).create().show();
            }
        });

        // Evento de swipe, scroll or click.
        dialogsListView.addOnItemTouchListener(new SwipeListener());
    }


    void buildListView(List<QBDialog> dialogs) {
        adapter.setDataSource(dialogs);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_chat) {

            // go to New Dialog activity
            //
            Intent intent = new Intent(DialogsActivity.this, NewDialogActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_add_user) {

            // go to New Dialog activity
            //
            Intent intent = new Intent(DialogsActivity.this, RegistrationActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
