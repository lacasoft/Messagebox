package com.globant.message.box.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.module.chat.model.QBChatHistoryMessage;
import com.quickblox.module.chat.model.QBMessage;
import com.globant.message.box.MessageboxSingleton;
import com.globant.message.box.R;

import java.util.Date;
import java.util.List;

public class ChatAdapter extends BaseAdapter {

    private static final String DATE_FORMAT = "dd-MM-yyyy hh:mm:ss";
    private final List<QBMessage> chatMessages;
    private Activity context;

    public ChatAdapter(Activity context, List<QBMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public QBMessage getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        QBMessage chatMessage = getItem(position);

        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = vi.inflate(R.layout.list_item_message, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        boolean isCurrent = false;

        if (chatMessage.getSenderId() != null) {

            int who = chatMessage.getSenderId();
            int whoCurrent = ((MessageboxSingleton) context.getApplication()).getCurrentUser().getId();

            isCurrent = who == whoCurrent ? false : true;
        }

        setAlignment(holder, isCurrent);

        holder.txtMessage.setText(chatMessage.getBody());

        if (chatMessage.getSenderId() != null) {
            holder.txtInfo.setText(chatMessage.getSenderId() + ": " + getTimeText(chatMessage));
        } else {
            holder.txtInfo.setText(getTimeText(chatMessage));
        }

        return convertView;
    }

    public void add(QBMessage message) {
        chatMessages.add(message);
    }

    public void remove(int positions) {
        // To DO
    }

    public void add(List<QBMessage> messages) {
        chatMessages.addAll(messages);
    }

    private void setAlignment(ViewHolder holder, boolean isIncoming) {

        if (isIncoming) {
            holder.contentWithBG.setBackgroundResource(R.drawable.incoming_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfo.setLayoutParams(layoutParams);
        } else {
            holder.contentWithBG.setBackgroundResource(R.drawable.outgoing_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtInfo.setLayoutParams(layoutParams);
        }
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        return holder;
    }

    private String getTimeText(QBMessage message) {
        if (message instanceof QBChatHistoryMessage){
            //Log.i("Chat Activity", "id: " + ((QBChatHistoryMessage)message).getDateSent());

            long time = ((QBChatHistoryMessage)message).getDateSent() * (long) 1000;
            Date date = new Date(time);

            return DateFormat.format(DATE_FORMAT, date).toString();
        }
        return DateFormat.format(DATE_FORMAT, new Date().getTime()).toString();
    }

    private static class ViewHolder {
        public TextView txtMessage;
        public TextView txtInfo;
        public LinearLayout content;
        public LinearLayout contentWithBG;
    }
}
