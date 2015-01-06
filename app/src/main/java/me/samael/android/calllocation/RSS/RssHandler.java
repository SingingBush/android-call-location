package me.samael.android.calllocation.RSS;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * @author Frank Ableson (fableson@msiservices.com)
 * This code is from the RSS feed article on the IBM website
 *
 */
public class RssHandler extends DefaultHandler {
	
	RssFeed _feed;
    RssItem _item;
    String _lastElementName = "";
    boolean bFoundChannel = false;
    final int RSS_TITLE = 1;
    final int RSS_LINK = 2;
    final int RSS_DESCRIPTION = 3;
    final int RSS_CATEGORY = 4;
    final int RSS_PUBDATE = 5;
    
    int depth = 0;
    int currentstate = 0;
    
    public RssHandler() {
    }
    
    /*
     * getFeed - this returns our feed when all of the parsing is complete
     */
    public RssFeed getFeed() {
        return _feed;
    }
    
    
    public void startDocument() throws SAXException {
        // initialize our RSSFeed object - this will hold our parsed contents
        _feed = new RssFeed();
        // initialize the RSSItem object - you will use this as a crutch to grab 
		// the info from the channel
        // because the channel and items have very similar entries..
        _item = new RssItem();
    }
    
    public void endDocument() throws SAXException {
    }
    
    public void startElement(String namespaceURI, String localName,String qName, Attributes atts) throws SAXException {
        depth++;
        if (localName.equals("channel")) {
            currentstate = 0;
            return;
        }
        
        if (localName.equals("image")) {
            // record our feed data - you temporarily stored it in the item :)
            _feed.setTitle(_item.getTitle());
            _feed.setPubDate(_item.getPubDate());
        }
        if (localName.equals("item")) {
            // create a new item
            _item = new RssItem();
            return;
        }
        
        if (localName.equals("title")) {
            currentstate = RSS_TITLE;
            return;
        }
        
        if (localName.equals("description")) {
            currentstate = RSS_DESCRIPTION;
            return;
        }
        
        if (localName.equals("link")) {
            currentstate = RSS_LINK;
            return;
        }
        
        if (localName.equals("category")) {
            currentstate = RSS_CATEGORY;
            return;
        }
        
        if (localName.equals("pubDate")) {
            currentstate = RSS_PUBDATE;
            return;
        }
        // if you don't explicitly handle the element, make sure you don't wind 
               // up erroneously storing a newline or other bogus data into one of our 
               // existing elements
        currentstate = 0;
    }
    
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        depth--;
        if (localName.equals("item")) {
            // add our item to the list!
            _feed.addItem(_item);
            return;
        }
    }
     
    public void characters(char ch[], int start, int length) {
        String theString = new String(ch,start,length);
        Log.i("RSSReader","characters[" + theString + "]");
        
        switch (currentstate) {
            case RSS_TITLE:
                _item.setTitle(theString);
                currentstate = 0;
                break;
            case RSS_LINK:
                _item.setLink(theString);
                currentstate = 0;
                break;
            case RSS_DESCRIPTION:
                _item.setDescription(theString);
                currentstate = 0;
                break;
            case RSS_CATEGORY:
                _item.setCategory(theString);
                currentstate = 0;
                break;
            case RSS_PUBDATE:
                _item.setPubDate(theString);
                currentstate = 0;
                break;
            default:
                return;
        }
        
    }

}
