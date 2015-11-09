package com.taylorandtucker.jot.ui;

/**
 * Created by tuckerkirven on 11/8/15.
 */
class FeedBackAsync  {

    private String entryID;
    private String message;

    public FeedBackAsync(String message) {
        this.message = message;
    }
/*
    protected Void doInBackground(Void... param) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://54.173.123.6:8000/feedback");

        try {
            HttpEntity entity = new ByteArrayEntity(message.getBytes("UTF-8"));
            httppost.setEntity(entity);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

        } catch (ClientProtocolException e) {
            System.out.println(e);
            return null;
            // TODO Auto-generated catch block
        } catch (IOException e) {
            System.out.println(e);
            return null;
            // TODO Auto-generated catch block
        }

        return null;
    }
    */
}


