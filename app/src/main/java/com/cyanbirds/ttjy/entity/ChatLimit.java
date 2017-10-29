package com.cyanbirds.ttjy.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;

/**
 * 作者：wangyb
 * 时间：2017/10/28 16:23
 * 描述：聊天次数限制
 */
@Entity
public class ChatLimit {

	@Id(autoincrement = true)
	public Long id;

	/**聊天对象用户id**/
	@Property
	@Unique
	@NotNull
	public String userId;

	/**聊天次数**/
	@Property
	@NotNull
	public int count;

	@Generated(hash = 144366648)
	public ChatLimit(Long id, @NotNull String userId, int count) {
		this.id = id;
		this.userId = userId;
		this.count = count;
	}

	@Generated(hash = 352419029)
	public ChatLimit() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
