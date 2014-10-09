package com.globant.message.box.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.globant.message.box.MessageboxSingleton;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.users.model.QBUser;
import com.globant.message.box.R;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends BaseAdapter {

    private List<QBUser> dataSource;
    private LayoutInflater inflater;
    private List<QBUser> selected = new ArrayList<QBUser>();

    public UsersAdapter(List<QBUser> dataSource, Context ctx) {
        this.dataSource = dataSource;
        this.inflater = LayoutInflater.from(ctx);
    }

    public List<QBUser> getSelected() {
        return selected;
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_user, null);
            holder = new ViewHolder();
            holder.login = (TextView) convertView.findViewById(R.id.userLogin);
            holder.add = (CheckBox) convertView.findViewById(R.id.addCheckBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBUser user = dataSource.get(position);

        int whoCurrent = ((MessageboxSingleton) inflater.getContext().getApplicationContext()).getCurrentUser().getId();

        if (user != null) {
            holder.login.setText(user.getLogin());

            if(user.getId() != whoCurrent) {
                holder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ((((CheckBox) v).isChecked())) {
                            selected.add(user);
                        } else {
                            selected.remove(user);
                        }
                    }
                });
                holder.add.setChecked(selected.contains(user));
            } else {
                holder.login.setText(user.getLogin() + " (" + inflater.getContext().getApplicationContext().getResources().getString(R.string.chat_me_title) + ")");

                holder.add.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView login;
        CheckBox add;
    }
}
