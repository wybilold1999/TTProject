package com.cyanbirds.ttjy.config;

import com.xiaomi.account.openauth.XiaomiOAuthConstants;

/**
 * 
 * @ClassName:Constants
 * @Description:定义全局常量
 * @Author:wangyb
 * @Date:2015年5月12日上午9:26:45
 *
 */
public class AppConstants {
	
	public static final String BASE_URL = "http://120.76.54.8/TTLoveServer/";
//	public static final String BASE_URL = "http://192.168.1.101/TTLoveServer/";

	/**
	 * 密码加密密匙
	 */
	public static final String SECURITY_KEY = "ABCD1234abcd5678";

	/**
	 *容联云IM
	 */
	public static final String YUNTONGXUN_ID = "8aaf07085a3c0ea1015a50eaef9408e5";
	public static final String YUNTONGXUN_TOKEN = "7010bb5f320ae628102e25518f5b45f2";

	/**
	 * QQ登录的appid和appkey
	 */
	public static final String mAppid = "1105980496";

	/**
	 * 微信登录
	 */
	public static final String WEIXIN_ID = "wx86ced05dbee0ad6b";

	/**
	 * 短信
	 */
	public static final String SMS_INIT_KEY = "1b6b963e439a6";
	public static final String SMS_INIT_SECRET = "f3a053ac5f1b4f592823ce53a0585920";

	/**
	 * 小米推送appid
	 */
	public static final String MI_PUSH_APP_ID = "2882303761517549515";
	/**
	 * 小米推送appkey
	 */
	public static final String MI_PUSH_APP_KEY = "5841754946515";

	public static final String MI_ACCOUNT_REDIRECT_URI = "http://www.cyanbirds.cn";

	public static final int[] MI_SCOPE = new int[]{XiaomiOAuthConstants.SCOPE_PROFILE, XiaomiOAuthConstants.SCOPE_OPEN_ID};

	/**
	 * 阿里图片节点
	 */
	public static final String OSS_IMG_ENDPOINT = "http://real-love-server.img-cn-shenzhen.aliyuncs.com/";

	public static final String WX_PAY_PLATFORM = "wxpay";

	public static final String ALI_PAY_PLATFORM = "alipay";

	public static final String MZ_APP_ID = "110467";
	public static final String MZ_APP_KEY = "74e7258593474bb0a04a91239243105b";

}
