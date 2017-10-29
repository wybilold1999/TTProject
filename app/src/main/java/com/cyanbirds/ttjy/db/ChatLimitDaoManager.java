package com.cyanbirds.ttjy.db;

import android.content.Context;

import com.cyanbirds.ttjy.db.base.DBManager;
import com.cyanbirds.ttjy.entity.ChatLimit;
import com.cyanbirds.ttjy.greendao.ChatLimitDao;

import org.greenrobot.greendao.query.QueryBuilder;

/**
 * 作者：wangyb
 * 时间：2016/12/29 11:49
 * 描述：
 */
public class ChatLimitDaoManager extends DBManager {

	private static ChatLimitDaoManager mInstance;
	private ChatLimitDao mChatLimitDao;
	private Context mContext;

	private ChatLimitDaoManager(Context context) {
		super(context);
		mContext = context;
		mChatLimitDao = getDaoSession().getChatLimitDao();
	}

	public static ChatLimitDaoManager getInstance(Context context) {
		if (mInstance == null) {
			synchronized (ChatLimitDaoManager.class) {
				if (mInstance == null) {
					mInstance = new ChatLimitDaoManager(context);
				}
			}
		}
		return mInstance;
	}

	public void insertOrReplace(ChatLimit chatLimit) {
		mChatLimitDao.insertOrReplace(chatLimit);
	}

	/**
	 * 根据用户id查询和该用户的聊天次数
	 * @param userId
	 * @return
	 */
	public ChatLimit getChatLimitByUid(String userId) {
		QueryBuilder<ChatLimit> qb = mChatLimitDao.queryBuilder();
		qb.where(ChatLimitDao.Properties.UserId.eq(userId));
		return qb.unique();
	}

	/**
	 * 退出的时候调用此方法
	 */
	public static void reset() {
		release();
		mInstance = null;
	}

}
