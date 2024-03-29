package com.xmpp.adapter;

import java.util.List;

import com.xmpp.R;
import com.xmpp.bean.Session;
import com.xmpp.util.Const;
import com.xmpp.view.CircleImageView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SessionAdapter extends BaseAdapter {
	private Context mContext;
	private List<Session> lists;

	public SessionAdapter(Context context, List<Session> lists) {
		this.mContext = context;
		this.lists = lists;
	}

	@Override
	public int getCount() {
		if (lists != null) {
			return lists.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return lists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Holder holder;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.fragment_news_item,null);
			holder = new Holder();
			holder.iv = (CircleImageView) convertView.findViewById(R.id.user_head);
			holder.tv_name = (TextView) convertView.findViewById(R.id.user_name);
			holder.tv_tips = (TextView) convertView.findViewById(R.id.tips);
			holder.tv_content = (TextView) convertView.findViewById(R.id.content);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		Session session = lists.get(position);
		if(session.getType().equals(Const.MSG_TYPE_ADD_FRIEND)){
			holder.tv_tips .setVisibility(View.VISIBLE);
			holder.iv.setImageResource(R.drawable.ibl);
		}else{
			holder.tv_tips .setVisibility(View.GONE);
			holder.iv.setImageResource(R.drawable.ic_launcher);
		}

		holder.tv_name.setText(session.getFrom());
		holder.tv_content.setText(session.getContent());
		holder.tv_time.setText(session.getTime());
		return convertView;
	}

	class Holder {
		CircleImageView iv;
		TextView tv_name,tv_tips;
		TextView tv_content;
		TextView tv_time;
	}

}
