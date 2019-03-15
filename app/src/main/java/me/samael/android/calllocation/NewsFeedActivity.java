package me.samael.android.calllocation;

import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import me.samael.android.calllocation.RSS.RssFeed;
import me.samael.android.calllocation.RSS.RssHandler;
import me.samael.android.calllocation.RSS.RssItem;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NewsFeedActivity extends ListActivity {
	
	private static final String TAG = NewsFeedActivity.class.getName();
	private ProgressDialog progressBar;

	private DownLoadNewsFeed _downLoadNewsFeedTask;

	private RssFeed rssFeed = null;
	private ArrayList<RssItem> rssItems;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.callhistory);

		_downLoadNewsFeedTask = new DownLoadNewsFeed();
		_downLoadNewsFeedTask.execute("https://feeds.bbci.co.uk/news/technology/rss.xml"); // todo replace URL with the projects RSS feed
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		if(AsyncTask.Status.RUNNING.equals(_downLoadNewsFeedTask.getStatus())) {
		    _downLoadNewsFeedTask.cancel(true);
        }
		super.onDestroy();
	}
	
	class RssArrayAdapter extends ArrayAdapter<RssItem> {
		Activity context;
		
		RssArrayAdapter(Activity context) {
			super(context, R.layout.newsfeed, rssItems);
			this.context = context;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if(row==null) {
				LayoutInflater inflator = context.getLayoutInflater();
				row = inflator.inflate(R.layout.newsfeed, null);
			}
			RssItem _item = rssItems.get(position);
			
			TextView title = (TextView) row.findViewById(R.id.newsfeed_title);
			title.setText(_item.getTitle());
			
			TextView content = (TextView) row.findViewById(R.id.newsfeed_content);
			content.setText(_item.getDescription());
			
			TextView date = (TextView) row.findViewById(R.id.newsfeed_date);
			date.setText(_item.getPubDate());
			
			return (row);
		}
	}
	
	private RssFeed getFeed(String urlOfRssFeed) {
		try {
			URL url = new URL(urlOfRssFeed);

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();

			XMLReader xmlreader = parser.getXMLReader();
			RssHandler theRssHandler = new RssHandler();
			
			xmlreader.setContentHandler(theRssHandler);
			
			InputSource inputSource = new InputSource(url.openStream());
			
			xmlreader.parse(inputSource); // perform the synchronous parse
			
			return theRssHandler.getFeed(); // returns populated RssFeed instance or null
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
	}
	
	class DownLoadNewsFeed extends AsyncTask<String, Integer, RssFeed> {
		
		@Override
		protected RssFeed doInBackground(String... params) {
			rssFeed = new RssFeed();
			try {
				rssFeed = getFeed(params[0]);
			} catch (Exception e) {
				rssFeed = null;
			}
			return rssFeed;
		}
		
		@Override
		protected void onPreExecute() {
			showProgressBar();
		}
		
		@Override
		protected void onPostExecute(RssFeed _feed) {
			if(rssFeed != null) {
				hideProgressBar();
				rssItems = _feed.getAllItems();
				setListAdapter(new RssArrayAdapter(NewsFeedActivity.this));
			}
		}

	}
	
	private void showProgressBar() {
		progressBar = new ProgressDialog(NewsFeedActivity.this);
		progressBar.setMessage(getText(R.string.newsfeedactivity_wait));
		progressBar.setCancelable(true);
		progressBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				_downLoadNewsFeedTask.cancel(true);
			}
		});
		progressBar.show();
	}
	
	private void hideProgressBar() {
		progressBar.hide();
		progressBar.dismiss();
	}
}
