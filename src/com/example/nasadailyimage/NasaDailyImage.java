package com.example.nasadailyimage;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class NasaDailyImage extends Activity {
	private static final String TAG = "NDI.MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nasa_daily_image);
        
        IotdHandler handler = new IotdHandler();
        try {
        	// Get the asynchronous task to conduct I/O
        	new DownloadFeedTask().execute( handler );
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_nasa_daily_image, menu);
        return true;
    }

    
    private class DownloadFeedTask extends AsyncTask<IotdHandler, Void, Void> {
    	private IotdHandler myHandler = null;

    	// Return type must be Void for Override to work and eclipse not complain
		@Override
		protected Void doInBackground( IotdHandler... handlerArray) {
			myHandler = handlerArray[0];
			
			try {
				// Get the RSS feed in the async thread
				myHandler.processFeed();
				
				// Also get the bitmap via HTTP connection
				myHandler.downloadBitmap( myHandler.getUrl() );
			} catch (Exception e) {
				Log.e( TAG, "DownloadTask::doInBackground" + e.toString());
			}
			return null;
		}
		
		// Need to override and the param from AsynTask< blah, blah, Void> must match the argument of onPostExecute()
		// http://stackoverflow.com/questions/3606505/onpostexecute-not-called-after-completion-asynctask
		//
		@Override
	    protected void onPostExecute( Void result ) {
	    	Log.i( TAG, "in post execute");
        	resetDisplay( myHandler.getTitle(), myHandler.getDate(), myHandler.getBitmap(), myHandler.getDescription() );
        	      	
	    }
    }
    
	
	private void resetDisplay( String title, String date, Bitmap imageBitmap, String description) {
		TextView titleView = (TextView)findViewById( R.id.imageTitle);
		titleView.setText( title );
		
		TextView dateView = (TextView)findViewById( R.id.imageDate);
		dateView.setText( date );
		
		ImageView imageView = (ImageView)findViewById( R.id.imageDisplay);
		// this involves network op and need to be in background task
		//
		imageView.setImageBitmap( imageBitmap );
		
		TextView descView = (TextView)findViewById( R.id.imageDesc);
		descView.setText( description );
		
	}
}
