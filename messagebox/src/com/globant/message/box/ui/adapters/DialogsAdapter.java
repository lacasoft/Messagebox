package com.globant.message.box.ui.adapters;

import android.app.Activity;
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

public class DialogsAdapter extends BaseAdapter {

    private static final String DATE_FORMAT = "dd-MM-yyyy hh:mm:ss";
    private List<QBDialog> dataSource;
    private LayoutInflater inflater;
    private Activity ctx;

    private int totalUnread = 0;

    public DialogsAdapter(List<QBDialog> dataSource, Activity ctx) {
        this.dataSource = dataSource;
        this.inflater = LayoutInflater.from(ctx);
        this.ctx = ctx;
    }

    public List<QBDialog> getDataSource() {
        return dataSource;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    public int getTotalUnread() {
        return this.totalUnread;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // init view
        //
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_room, null);
            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.roomName);
            holder.lastMessage = (TextView)convertView.findViewById(R.id.lastMessage);
            holder.groupType = (TextView)convertView.findViewById(R.id.textViewGroupType);
            holder.dateSent = (TextView)convertView.findViewById(R.id.textViewDateSent);
            holder.unread = (TextView)convertView.findViewById(R.id.textViewUnread);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // set data
        //
        QBDialog dialog = dataSource.get(position);
        if(dialog.getType().equals(QBDialogType.GROUP)){
            holder.name.setText(dialog.getName());
        }else{
            // get opponent name for private dialog
            //
            Integer opponentID = ((MessageboxSingleton)ctx.getApplication()).getOpponentIDForPrivateDialog(dialog);
            QBUser user = ((MessageboxSingleton)ctx.getApplication()).getDialogsUsers().get(opponentID);
            if(user != null){
                holder.name.setText(user.getLogin() == null ? user.getFullName() : user.getLogin());
            }
        }

        holder.lastMessage.setText(dialog.getLastMessage());
        holder.groupType.setText(dialog.getType().toString());

        holder.dateSent.setText(getTimeText(dialog.getLastMessageDateSent()).toString());
        holder.unread.setText(dialog.getUnreadMessageCount().toString());

        this.totalUnread += dialog.getUnreadMessageCount();

        return convertView;
    }

    private String getTimeText(long dateTime) {

            long time = dateTime * (long) 1000;
            Date date = new Date(time);

            return DateFormat.format(DATE_FORMAT, date).toString();
    }

    private static class ViewHolder{
        TextView name;
        TextView lastMessage;
        TextView groupType;
        TextView dateSent;
        TextView unread;
    }
}
