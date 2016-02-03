package com.example.monicamarcus.musicplayer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import dalvik.annotation.TestTarget;

import static junit.framework.Assert.assertEquals;

public class MainActivity extends Activity {
    private static String endpoint = "http://streaming.earbits.com/api/v1/track.json?stream_id=5654d7c3c5aa6e00030021aa";

    private static final String TAG_ARTIST_NAME = "artist_name";
    private static final String TAG_TRACK_NAME = "name";
    private static final String TAG_COVER_IMAGE_URL = "cover_image";

    TextView tvResponse;
    TextView tvIsConnected;
    ImageView coverImage;
    ProgressDialog mProgressDialog;
    String imageURL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResponse = (TextView) findViewById(R.id.tvResponse);
        tvIsConnected = (TextView) findViewById(R.id.tvIsConnected);
        coverImage = (ImageView) findViewById(R.id.image);

        assertEquals("text should be empty", "", tvResponse.getText().toString());

        // check if you are connected or not
        if(isConnected()){
            tvIsConnected.setBackgroundColor(0xFF00CC00);
            tvIsConnected.setText("You are connected");
        }
        else{
            tvIsConnected.setText("You are NOT connected");
        }

        // call AsynTask to perform network operation on separate thread
        //new HttpAsyncTask().execute(endpoint);
    }

    //When the Next Track button is clicked
    public void nextTrack(View view) {
        new HttpAsyncTask().execute(endpoint);
    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private class getImageAsyncTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Download Cover Image");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                Toast.makeText(getBaseContext(), "Image not available", Toast.LENGTH_LONG).show();
            }
            // Set the bitmap into ImageView
            coverImage.setImageBitmap(result);
            mProgressDialog.dismiss();
        }
    }


    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_SHORT).show();
            Log.v("RESULT",result);
            try {
                //JSONObject json = (JSONObject) new JSONTokener(result).nextValue();
                JSONObject json = new JSONObject(result);
                String text = "TRACK NAME: " + getTrackName(json) + "\n" + "ARTIST NAME: " + getArtistName(json);
                tvResponse.setText(text);
                //assertEquals("text should contain","TRACK NAME: ", tvResponse.getText().toString().substring(0,11));
                new getImageAsyncTask().execute(getCoverImageURL(json));
            } catch (JSONException e) {
                //e.printStackTrace();
                e.getMessage();
            }
        }
    }

    private String getTrackName(JSONObject json) {
        String s = null;
        try {
            s = json.getString(TAG_TRACK_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            return s;
        }
    }

    private String getArtistName(JSONObject json) {
        String s = null;
        try {
            //JSONArray artist = json.getJSONArray("artist");
            s = json.getString(TAG_ARTIST_NAME);
            //JSONObject c = json.getJSONObject("artist_name");
            //JSONArray c = artist.getJSONArray(0);
            // Storing  JSON item in a Variable
            //String id = c.getString(TAG_ID);
            //String name = c.getString(TAG_NAME);
            //String email = c.getString(TAG_EMAIL);
        } catch (JSONException e) {
            Log.v("EXCEPTION", e.getMessage());
            e.printStackTrace();
        }
        finally {
            return s;
        }
    }

    private String getCoverImageURL(JSONObject json){
        String imageURL = null;
        try {
            imageURL = json.getString(TAG_COVER_IMAGE_URL);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            return imageURL;
        }
    }

    }
