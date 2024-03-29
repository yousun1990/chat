package com.xmpp.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;

import com.xmpp.MyApplication;
import com.xmpp.R;
import com.xmpp.activity.ChatActivity;
import com.xmpp.adapter.SessionAdapter;
import com.xmpp.bean.Session;
import com.xmpp.db.SessionDao;
import com.xmpp.util.Const;
import com.xmpp.util.PreferencesUtils;
import com.xmpp.util.ToastUtil;
import com.xmpp.util.XmppUtil;
import com.xmpp.view.CustomListView;
import com.xmpp.view.TitleBarView;
import com.xmpp.view.CustomListView.OnRefreshListener;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class NewsFragment extends Fragment implements OnRefreshListener{
	private Context mContext;
	private View mBaseView;
	private CustomListView mCustomListView;
	private SessionAdapter adapter;
	private List<Session> sessionList = new ArrayList<Session>();
	private TitleBarView mTitleBarView;
	private SessionDao sessionDao;
	private String userid;

	private AddFriendReceiver addFriendReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mContext = getActivity();
		mBaseView = inflater.inflate(R.layout.fragment_news_father, null);
		userid=PreferencesUtils.getSharePreStr(mContext, "username");
		sessionDao=new SessionDao(mContext);
		addFriendReceiver=new AddFriendReceiver();
		IntentFilter intentFilter=new IntentFilter(Const.ACTION_ADDFRIEND);
		mContext.registerReceiver(addFriendReceiver, intentFilter);
		findView();
		initData();
		return mBaseView;
	}

	private void findView() {
		mTitleBarView=(TitleBarView) mBaseView.findViewById(R.id.title_bar);
		mTitleBarView.setCommonTitle(View.GONE, View.VISIBLE, View.GONE, View.GONE);
		mTitleBarView.setTitleText(R.string.msgs);
		
		mCustomListView = (CustomListView) mBaseView.findViewById(R.id.lv_news);//listview
		mCustomListView.setOnRefreshListener(this);//设置listview下拉刷新监听
		mCustomListView.setCanLoadMore(false);//设置禁止加载更多
		mCustomListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				final Session session=sessionList.get(arg2-1);
				if(session.getType().equals(Const.MSG_TYPE_ADD_FRIEND)){
					if(!TextUtils.isEmpty(session.getIsdispose())){
						if(!session.getIsdispose().equals("1")){
							Builder bd=new AlertDialog.Builder(mContext);
							bd.setItems(new String[]{"同意"}, new OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									Roster roster= MyApplication.xmppConnection.getRoster();
									XmppUtil.addGroup(roster, "我的好友");//先默认创建一个分组
									if(XmppUtil.addUsers(roster, session.getFrom()+"@"+ MyApplication.xmppConnection.getServiceName(), session.getFrom(),"我的好友")){
										//告知对方，同意添加其为好友
										new Thread(new Runnable() {
											@Override
											public void run() {
												try {
													//注意消息的协议格式 =》接收者卍发送者卍消息类型卍消息内容卍发送时间
													String message= session.getFrom()+Const.SPLIT+userid+Const.SPLIT+Const.MSG_TYPE_ADD_FRIEND_SUCCESS+Const.SPLIT+""+Const.SPLIT+new SimpleDateFormat("MM-dd HH:mm").format(new Date());
													XmppUtil.sendMessage(MyApplication.xmppConnection, message, session.getFrom());
												} catch (XMPPException e) {
													e.printStackTrace();
												}
											}
										}).start();
										sessionDao.updateSessionToDisPose(session.getId());//将本条数据在数据库中改为已处理
//										ToastUtil.showShortToast(mContext, "你们已经是好友了，快去聊天吧！");
										sessionList.remove(session);
										session.setIsdispose("1");
										sessionList.add(0,session);
										adapter.notifyDataSetChanged();
									}else{
										ToastUtil.showLongToast(mContext, "添加好友失败");
									}
								}
							});
							bd.create().show();
						}else{
							ToastUtil.showShortToast(mContext, "已同意");
						}
					}
				}else{
					Intent intent=new Intent(mContext, ChatActivity.class);
					intent.putExtra("from", session.getFrom());
					startActivity(intent);
				}
			}
		});
	}

	private void initData() {
		//注意，当数据量较多时，此处要开线程了，否则阻塞主线程
		sessionList=sessionDao.queryAllSessions(userid);
		adapter = new SessionAdapter(mContext, sessionList);
		mCustomListView.setAdapter(adapter);
	}

	@Override
	public void onRefresh() {
		 initData();
		mCustomListView.onRefreshComplete();
	}
	
	class AddFriendReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context arg0, Intent intent) {
			initData();
		}
		
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mContext.unregisterReceiver(addFriendReceiver);
	}

}
