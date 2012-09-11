package com.example.nasadailyimage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.text.SimpleDateFormat;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class IotdHandler extends DefaultHandler {
	private static final String TAG = "NDI.IotdHandler";
	 
	private String iotdUrl = "http://www.nasa.gov/rss/image_of_the_day.rss";
	private boolean inTitle = false;
	private boolean inDescription = false;
	private boolean inItem = false;
	private boolean inDate = false;

	private String imageUrl = null;
	private StringBuffer title = new StringBuffer();
	private StringBuffer description = new StringBuffer();
	private String date = null;
	private Bitmap myBitmap = null;
	
	public void processFeed() {
		try {
        	SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser sp = factory.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(this);
        	
        	// A form of parse() can take the URI instead of the result of openStream()
        	xr.parse( iotdUrl );
        
        	/*
        	InputStream ipStream = new URL( iotdUrl ).openStream();
            InputSource src = new InputSource(ipStream);
            xr.parse(src);
            */
        } catch (IOException e) {
            Log.e("IO", e.toString());
        } catch (SAXException e) {
            Log.e("SAX", e.toString());
        } catch (ParserConfigurationException e) {
            Log.e("Parser", e.toString());
        } catch (Exception e) {
        	// http://stackoverflow.com/questions/115008/how-can-we-print-line-numbers-to-the-log-in-java
        	//
        	String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        	int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
        	
        	Log.e( TAG, "processFeed exception" + methodName + ":" + lineNumber + " > " + e.toString());
        }
	}
	
	public void downloadBitmap(String url) {
		try {
			HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
			connection.setDoInput( true );
			connection.connect();
			
			InputStream input = connection.getInputStream();
			myBitmap = BitmapFactory.decodeStream( input);
			input.close();
		} catch (IOException ioe) {
			Log.e( TAG, "dnload bitmap err " + ioe.toString() + url);
		} 
	}

	public Bitmap getBitmap() {
		return myBitmap;
	}
	public String getUrl() {
		return imageUrl;
	}

	public String getTitle() {
		return title.toString();
	}

	public String getDescription() {
		return description.toString();
	}

	public String getDate() {
		return date;
	}

	public void characters(char ch[], int start, int length) {
		 String chars = (new String(ch).substring(start, start + length));

		 if (inTitle) { 
			 title.append(chars);
		 }

		 if (inDescription) { 
			 description.append(chars);
		 }

		 if (inDate && date == null) {
			 //Example: Tue, 21 Dec 2010 00:00:00 EST
			 String rawDate = chars;
			 try { 
				 SimpleDateFormat parseFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
				 Date sourceDate = (Date) parseFormat.parse(rawDate);

				 SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
				 date = outputFormat.format(sourceDate);
			 } catch (Exception e) { 
				 e.printStackTrace();
			 }
		 }
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (localName.equals("enclosure")) { 
			imageUrl = attributes.getValue("url");
			Log.i( TAG, "imageUrl=" + imageUrl);
		}  

		if (localName.startsWith("item")) { 
			inItem = true;
		} else { 
			if (inItem) { 
				if (localName.equals("title")) { 
					inTitle = true;
				} else { 
					inTitle = false;
				}

				if (localName.equals("description")) { 
					inDescription = true;
				} else { 
					inDescription = false;
				}

				if (localName.equals("pubDate")) { 
					inDate = true;
				} else { 
					inDate = false;
				}
			}
		}

	}
	
}
