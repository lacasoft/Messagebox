package com.globant.message.box.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.module.chat.model.QBChatHistoryMessage;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.module.chat.model.QBMessage;
import com.quickblox.module.users.model.QBUser;
import com.globant.message.box.MessageboxSingleton;
import com.globant.message.box.R;

import java.util.Date;
import java.util.List;

public class DialogsAdapter extends RecyclerView.Adapter<DialogsAdapter.ViewHolder> {
    private static final String DATE_FORMAT = "dd-MM-yyyy hh:mm:ss";

    private Activity ctx;
    private List<QBDialog> dataSource;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView unreadTextView;
        public TextView dateSentTextView;
        public TextView groupTypeTextView;
        public TextView lastMessageTextView;

        public ViewHolder(View view) {
            super(view);

            this.nameTextView = (TextView) view.findViewById(R.id.roomName);
            this.unreadTextView = (TextView) view.findViewById(R.id.textViewUnread);
            this.lastMessageTextView = (TextView) view.findViewById(R.id.lastMessage);
            this.dateSentTextView = (TextView) view.findViewById(R.id.textViewDateSent);
            this.groupTypeTextView = (TextView) view.findViewById(R.id.textViewGroupType);
        }
    }

    public DialogsAdapter(List<QBDialog> dataSource, Activity context) {
        this.ctx = context;
        this.dataSource = dataSource;
    }

    public List<QBDialog> getDataSource() {
        return dataSource;
    }

    public void setDataSource(List<QBDialog> dialogos) {
        this.dataSource = dialogos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.list_item_room, parent, false);

        ViewHolder holder = new ViewHolder(view);

        //view.setOnClickListener();

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        QBDialog dialog = dataSource.get(position);

        if (dialog.getType().equals(QBDialogType.GROUP)) {
            viewHolder.nameTextView.setText(dialog.getName());
        } else {
            Integer opponentID = ((MessageboxSingleton) ctx.getApplication()).getOpponentIDForPrivateDialog(dialog);
            QBUser user = ((MessageboxSingleton) ctx.getApplication()).getDialogsUsers().get(opponentID);

            if (user != null) {
                viewHolder.nameTextView.setText(user.getLogin() == null ? user.getFullName() : user.getLogin());
            }
        }

        viewHolder.lastMessageTextView.setText(dialog.getLastMessage());
        viewHolder.dateSentTextView.setText(getTimeText(dialog.getLastMessageDateSent()).toString());
        viewHolder.groupTypeTextView.setText(dialog.getType().toString());
        viewHolder.unreadTextView.setText(dialog.getUnreadMessageCount().toString());
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    private String getTimeText(long dateTime) {
        long time = dateTime * (long) 1000;
        Date date = new Date(time);

        return DateFormat.format(DATE_FORMAT, date).toString();
    }
}
