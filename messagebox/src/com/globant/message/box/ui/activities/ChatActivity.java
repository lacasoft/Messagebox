package com.globant.message.box.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.model.QBChatHistoryMessage;
import com.quickblox.module.chat.model.QBChatMarkersExtension;
import com.quickblox.module.chat.model.QBChatMessage;
import com.quickblox.module.chat.model.QBChatMessageExtension;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBMessage;
import com.globant.message.box.MessageboxSingleton;
import com.globant.message.box.R;
import com.globant.message.box.core.ChatManager;
import com.globant.message.box.core.GroupChatManagerImpl;
import com.globant.message.box.core.PrivateChatManagerImpl;
import com.globant.message.box.ui.adapters.ChatAdapter;
import com.quickblox.module.custom.model.QBCustomObject;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.DefaultPacketExtension;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends Activity {

    private static final String TAG = ChatActivity.class.getSimpleName();

    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_DIALOG = "dialog";
    private final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";
    private final String PROPERTY_ID_PRODUCT_EBAY = "id_product_ebay";

    private EditText messageEditText;
    private ListView messagesContainer;
    private Button sendButton;
    private ProgressBar progressBar;

    private Mode mode = Mode.PRIVATE;
    private ChatManager chat;
    private ChatAdapter adapter;
    private QBDialog dialog;

    private ArrayList<QBChatHistoryMessage> history;

    public static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initViews();
    }

    @Override
    public void onBackPressed() {
        try {
            chat.release();
        } catch (XMPPException e) {
            Log.e(TAG, getApplicationContext().getResources().getString(R.string.alert_error_release_chat), e);
        }
        super.onBackPressed();
    }

    private void initViews() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        sendButton = (Button) findViewById(R.id.chatSendButton);

        TextView meLabel = (TextView) findViewById(R.id.meLabel);
        TextView companionLabel = (TextView) findViewById(R.id.companionLabel);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Intent intent = getIntent();

        // Get chat dialog
        //
        dialog = (QBDialog)intent.getSerializableExtra(EXTRA_DIALOG);

        mode = (Mode) intent.getSerializableExtra(EXTRA_MODE);

        switch (mode) {
            case GROUP:
                chat = new GroupChatManagerImpl(this);
                container.removeView(meLabel);
                container.removeView(companionLabel);

                // Join group chat
                //
                progressBar.setVisibility(View.VISIBLE);
                //
                ((GroupChatManagerImpl) chat).joinGroupChat(dialog, new QBEntityCallbackImpl() {
                    @Override
                    public void onSuccess() {

                        // Load Chat history
                        //
                        loadChatHistory();
                    }

                    @Override
                    public void onError(List list) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
                        dialog.setMessage(getApplicationContext().getResources().getString(R.string.alert_error_join_group_chat) + list.toString()).create().show();
                    }
                });

                break;
            case PRIVATE:
                Integer opponentID = ((MessageboxSingleton)getApplication()).getOpponentIDForPrivateDialog(dialog);

                chat = new PrivateChatManagerImpl(this, opponentID);

                companionLabel.setText(((MessageboxSingleton)getApplication()).getDialogsUsers().get(opponentID).getLogin());

                // Load Chat history
                //
                loadChatHistory();
                break;
        }

        messageEditText.addTextChangedListener(new TextWatcher() {
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

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString();
                String idProducttEbay = "1234567890";

                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                // Send chat message
                //
                QBChatMessage chatMessage = new QBChatMessage();
                chatMessage.setBody(messageText);
                chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
                chatMessage.setProperty(PROPERTY_ID_PRODUCT_EBAY, idProducttEbay);

                try {
                    chat.sendMessage(chatMessage);
                } catch (XMPPException e) {
                    Log.e(TAG, getApplicationContext().getResources().getString(R.string.alert_error_send_message), e);
                } catch (SmackException sme){
                    Log.e(TAG, getApplicationContext().getResources().getString(R.string.alert_error_send_message), sme);
                }

                messageEditText.setText("");

                if(mode == Mode.PRIVATE) {
                    showMessage(chatMessage);
                }
            }
        });
    }

    public void enableSubmitIfReady() {

        boolean isReady = messageEditText.getText().toString().length()>0;

        if (isReady) {
            sendButton.setEnabled(true);
        } else {
            sendButton.setEnabled(false);
        }
    }

    private void loadChatHistory(){
        QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();
        customObjectRequestBuilder.setPagesLimit(100);

        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBChatHistoryMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatHistoryMessage> messages, Bundle args) {
                history = messages;

                adapter = new ChatAdapter(ChatActivity.this, new ArrayList<QBMessage>());
                messagesContainer.setAdapter(adapter);

                for(QBMessage msg : messages) {
                    showMessage(msg);
                }

                progressBar.setVisibility(View.GONE);

                messagesContainer.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                        return onLongListItemClick(v,pos,id);
                    }
                });
                //registerForContextMenu(messagesContainer);
            }

            @Override
            public void onError(List<String> errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
                dialog.setMessage(getApplicationContext().getResources().getString(R.string.alert_error_load_chat_history) + errors).create().show();
            }
        });
    }

    public void showMessage(QBMessage message) {
        adapter.add(message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                scrollDown();
            }
        });
    }

    private void scrollDown() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private void scrollUp() {
        messagesContainer.setSelection(messagesContainer.getCount() + 1);
    }

    protected boolean onLongListItemClick(View v, int pos, long id) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(getApplicationContext().getResources().getString(R.string.chat_ac_title));

        // set dialog message
        alertDialogBuilder
                .setMessage(getApplicationContext().getResources().getString(R.string.alert_info_title_delete))
                .setCancelable(false)
                .setPositiveButton(getApplicationContext().getResources().getString(R.string.alert_info_btn_yes),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        Toast.makeText(getApplicationContext(), "To Do " + getApplicationContext().getResources().getString(R.string.alert_info_title_delete), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(getApplicationContext().getResources().getString(R.string.alert_info_btn_no),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

        Log.i(TAG, "onLongListItemClick id=" + id);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

        menu.setHeaderTitle(getApplicationContext().getResources().getString(R.string.chat_ac_title));
        menu.add(0, (int) info.id,info.position,getApplicationContext().getResources().getString(R.string.chat_ac_delete));

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);

        if(item.getTitle().equals(getApplicationContext().getResources().getString(R.string.chat_ac_delete))) {
            Toast.makeText(this, "To Do " + getApplicationContext().getResources().getString(R.string.chat_ac_delete), Toast.LENGTH_LONG).show();
        }
        return true;
    };

    public static enum Mode {PRIVATE, GROUP}
}
