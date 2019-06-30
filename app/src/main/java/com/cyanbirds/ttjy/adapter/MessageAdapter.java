package com.cyanbirds.ttjy.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyanbirds.ttjy.R;
import com.cyanbirds.ttjy.activity.FConversationActivity;
import com.cyanbirds.ttjy.activity.PersonalInfoActivity;
import com.cyanbirds.ttjy.config.ValueKey;
import com.cyanbirds.ttjy.db.ConversationSqlManager;
import com.cyanbirds.ttjy.db.FConversationSqlManager;
import com.cyanbirds.ttjy.db.IMessageDaoManager;
import com.cyanbirds.ttjy.entity.Conversation;
import com.cyanbirds.ttjy.listener.MessageUnReadListener;
import com.cyanbirds.ttjy.manager.NotificationManager;
import com.cyanbirds.ttjy.utils.DateUtil;
import com.cyanbirds.ttjy.utils.EmoticonUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;


/**
 * @author Cloudsoar(wangyb)
 * @datetime 2015-12-26 15:24 GMT+8
 * @email 395044952@qq.com
 */
public class MessageAdapter extends
        RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Conversation> mConversations;
    private Context mContext;

    public MessageAdapter(Context context, List<Conversation> mConversations) {
        this.mConversations = mConversations;
        this.mContext = context;
    }

    @Override
    public int getItemCount() {
        return mConversations == null ? 0 : mConversations.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Conversation conversation = mConversations.get(position);
        if(!TextUtils.isEmpty(conversation.localPortrait)){
            if (conversation.localPortrait.startsWith("res://")) {//官方头像
                holder.mPortrait.setImageURI(Uri.parse(conversation.localPortrait));
            } else {
                holder.mPortrait.setImageURI(Uri.parse("file://" + conversation.localPortrait));
            }
        }
        holder.mTitle.setText(conversation.talkerName);
        holder.mContent.setText(Html.fromHtml(
                EmoticonUtil.convertExpression(conversation.content==null ? "" : conversation.content),
                EmoticonUtil.conversation_imageGetter_resource, null));

        holder.mUpdateTime.setText(DateUtil.longToString(conversation.createTime));
        holder.mUnreadCount.setVisibility(View.GONE);
        if (conversation.unreadCount != 0) {
            holder.mUnreadCount.setVisibility(View.VISIBLE);
            if (conversation.unreadCount >= 100) {
                holder.mUnreadCount.setText(String.valueOf("99+"));
            } else {
                holder.mUnreadCount.setText(String
                        .valueOf(conversation.unreadCount));
            }
        }
        if (!TextUtils.isEmpty(conversation.channel)) {
            holder.mChannel.setText(Html.fromHtml(String.format(mContext.getResources().getString(R.string.channel), conversation.channel)));
        } else {
            holder.mChannel.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(conversation.city)) {
            holder.mCity.setText(Html.fromHtml(String.format(mContext.getResources().getString(R.string.city), conversation.city)));
        } else {
            holder.mCity.setVisibility(View.GONE);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_message, parent, false));
    }


    class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener, View.OnLongClickListener{
        SimpleDraweeView mPortrait;
        TextView mUnreadCount;
        ImageView mRedPoint;
        TextView mTitle;
        TextView mUpdateTime;
        TextView mContent;
        TextView mChannel;
        TextView mCity;
        LinearLayout mItemMsg;

        public ViewHolder(View itemView) {
            super(itemView);
            mPortrait = (SimpleDraweeView) itemView.findViewById(R.id.portrait);
            mUnreadCount = (TextView) itemView.findViewById(R.id.un_read_number);
            mRedPoint = (ImageView) itemView.findViewById(R.id.red_point);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mUpdateTime = (TextView) itemView.findViewById(R.id.update_time);
            mContent = (TextView) itemView.findViewById(R.id.content);
            mChannel = (TextView) itemView.findViewById(R.id.tv_channel);
            mCity = (TextView) itemView.findViewById(R.id.tv_city);
            mItemMsg = (LinearLayout) itemView.findViewById(R.id.item_msg);
            mItemMsg.setOnClickListener(this);
            mItemMsg.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (mConversations.size() > position && position > -1) {
                Conversation conversation = mConversations.get(position);
                Intent intent = new Intent(mContext, FConversationActivity.class);
                intent.putExtra(ValueKey.CONVERSATION, conversation);
                mContext.startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            final int position = getAdapterPosition();
            new AlertDialog.Builder(mContext)
                    .setItems(
                            new String[] {
                                    mContext.getResources().getString(
                                            R.string.delete_conversation),
                                    mContext.getResources().getString(
                                            R.string.delete_all_conversation),
                                    mContext.getResources().getString(
                                            R.string.check_personal_info)},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            Conversation conversation = mConversations.get(position);
                                            ConversationSqlManager.getInstance(mContext).deleteConversationById(conversation);
                                            FConversationSqlManager.getInstance(mContext).deleteAllConversationByRid(conversation.id);
                                            IMessageDaoManager.getInstance(mContext).deleteIMessageByConversationId(conversation.id);
                                            mConversations.remove(conversation);
                                            notifyDataSetChanged();
                                            MessageUnReadListener.getInstance().notifyDataSetChanged(0);
                                            break;
                                        case 1:
                                            ConversationSqlManager.getInstance(mContext).deleteAllConversation();
                                            FConversationSqlManager.getInstance(mContext).deleteAllConversation();
                                            IMessageDaoManager.getInstance(mContext).deleteAllIMessage();
                                            mConversations.clear();
                                            notifyDataSetChanged();
                                            MessageUnReadListener.getInstance().notifyDataSetChanged(0);
                                            NotificationManager.getInstance().cancelNotification();
                                            break;
                                        case 2:
                                            Intent intent = new Intent(mContext, PersonalInfoActivity.class);
                                            intent.putExtra(ValueKey.USER_ID, mConversations.get(position).talker);
                                            mContext.startActivity(intent);
                                            break;
                                    }
                                    dialog.dismiss();

                                }
                            }).setTitle("操作").show();
            return true;
        }
    }

    public void setConversations(List<Conversation> conversations){
        this.mConversations = conversations;
    }
}
