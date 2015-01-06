package me.samael.android.calllocation.RSS;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author Frank Ableson (fableson@msiservices.com)
 * This code is from Frank Ablesons article on the IBM website
 *
 */
public class RssFeed {
	private String _title = null;
	private String _pubdate = null;
	private int _itemcount = 0;
	private ArrayList<RssItem> _itemlist;

	public RssFeed() {
		_itemlist = new ArrayList<RssItem>();
	}
	
	int addItem(RssItem item) {
		_itemlist.add(item);
		_itemcount++;
		return _itemcount;
	}
	
	RssItem getItem(int location) {
		return _itemlist.get(location);
	}
	
	public ArrayList<RssItem> getAllItems() {
		return _itemlist;
	}
	
	int getItemCount() {
		return _itemcount;
	}
	
	void setTitle(String title) {
		_title = title;
	}
	
	void setPubDate(String pubdate) {
		_pubdate = pubdate;
	}
	
	public String getTitle() {
		return _title;
	}
	
	public String getPubDate() {
		return _pubdate;
	}
}
