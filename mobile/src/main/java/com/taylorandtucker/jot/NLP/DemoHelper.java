package com.taylorandtucker.jot.NLP;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;

import com.taylorandtucker.jot.Entry;
import com.taylorandtucker.jot.R;
import com.taylorandtucker.jot.ui.SentimentGraphFragment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Created by tuckerkirven on 10/11/15.
 */
public class DemoHelper {

    String entryNeutralShort = "this is supposed to be a neutral sentence. ";
    String entryNeutralLong = "This is supposed to be a neutral sentence with more length. These are average words. ";

    String entryNegativeShort = "This is bad. ";
    String entryNegativeLong = "This is the worst thing ever! I hate this so much! ";

    String entryPositiveShort = "This is good. ";
    String entryVeryPositiveShort = "This is great! ";

    String a = entryNeutralShort;
    String b = entryNeutralLong;
    String c = entryNegativeShort;
    String d = entryPositiveShort;
    String e = entryVeryPositiveShort;
    String f = entryNegativeLong;

    Activity activity;

    SentimentGraphFragment sgChart;

    public DemoHelper(Activity activity){
        this.activity = activity;
        entriesFromFile();
    }
    public DemoHelper(int numEntries, int avgTimeSecBetween, Activity activity, SentimentGraphFragment sgChart) {
        this.activity = activity;
        this.sgChart = sgChart;

        List entries = Arrays.asList(a, b, c, d, e, f);

        long nowSec = new Date().getTime() / 1000;
        List fakeEntries = new ArrayList();
        List dates = new ArrayList();
        Random rand = new Random();
        for (int i = 0; i < numEntries; i++) {
            int randomNum = rand.nextInt(4)+1;
            String entry = "";
            for (int j = 0; j < randomNum; j++) {
                int randSent = rand.nextInt((entries.size()));
                entry += entries.get(randSent);
            }
            //fakeEntries.add(entry);

            long nextTime = nowSec - rand.nextInt(avgTimeSecBetween) - avgTimeSecBetween / 2;
            System.out.println("nextTime: " + nextTime);
            //dates.add(nextTime);
            nowSec = nextTime;

            process(entry, nextTime);
        }


    }
    public void entriesFromFile(){

        try (BufferedReader br = new BufferedReader(new InputStreamReader(activity.getResources().openRawResource(R.raw.fake_entries)))) {
            String line;
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm");
            while ((line = br.readLine()) != null) {

                if(line.length() >=2){
                    if(line.startsWith("*")){
                        date = df.parse(line.substring(1,line.length()));
                    }else{
                        process(line, date);
                    }
                }
            }
        }catch (Exception e){
            System.out.println("FILE MESS: "+ e);
        }
    }

    public void process(String entryText, long dateSec) {
        final Entry entry = new Entry(new Date(dateSec * 1000), entryText);

        InfoExtractor ie = new InfoExtractor(activity);

        Uri uri = ie.putEntry(entry);
        String[] segments = uri.getPath().split("/");
        String idStr = segments[segments.length - 1];

        RetrieveNLPdata nlp = new RetrieveNLPdata(idStr, entry.getBody());
        nlp.execute();
    }
    public void process(String entryText, Date date) {
        final Entry entry = new Entry(date, entryText);

        InfoExtractor ie = new InfoExtractor(activity);

        Uri uri = ie.putEntry(entry);
        String[] segments = uri.getPath().split("/");
        String idStr = segments[segments.length - 1];

        RetrieveNLPdata nlp = new RetrieveNLPdata(idStr, entryText);
        nlp.execute();
    }

    class RetrieveNLPdata extends AsyncTask<Void, Void, Void> {

        private String entryID;
        private String entry;

        public RetrieveNLPdata(String entryID, String entry) {
            this.entryID = entryID;
            this.entry = entry;
        }

        protected Void doInBackground(Void... param) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://54.173.123.6:8000/entry");

            try {
                HttpEntity entity = new ByteArrayEntity(entry.getBytes("UTF-8"));
                httppost.setEntity(entity);

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                String xml = "";


                while ((line = rd.readLine()) != null) {
                    xml += line;
                }

                final ProcessedEntry ent = new ProcessedEntry(xml, entry, true);


                InfoExtractor ie = new InfoExtractor(activity);
                ie.processNewEntryData(Long.parseLong(entryID), ent);



                final List entries = ie.getAllEntries();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(sgChart != null) {
                            sgChart.updateData(entries);
                        }
                    }
                });

                String entryText = ie.getEntryById(Long.parseLong(entryID)).getBody();

                Map<String, Double> a = ent.personSentiment();
                double b = ent.getEntrySentiment();
                System.out.println("===============================================================");
                System.out.println(entryText);
                System.out.print("zzzENTRY CUM SUM: ");
                System.out.println(b);
                System.out.print("IND SENT SENT: ");
                for (double val : ent.getSentenceSentiments(false)) {
                    System.out.print(val);
                }
                System.out.println();
                System.out.print("ENTITIES: ");
                for (Map.Entry<String, Double> entry : a.entrySet()) {
                    System.out.print(entry.getKey() + " : " + entry.getValue());
                }
                System.out.println();



                System.out.println("===================== ENTRIES =======================");

                /*
                    DBUtils utils = DBUtils.getInstance(getContext());
                    cardCursorAdapter.swapCursor(utils.getAllEntriesQuery());
                */

                return null;

            } catch (ClientProtocolException e) {
                System.out.println(e);
                return null;
                // TODO Auto-generated catch block
            } catch (IOException e) {
                System.out.println(e);
                return null;
                // TODO Auto-generated catch block
            }

        }
    }


}
