package com.taylorandtucker.jot.NLP;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;

import com.taylorandtucker.jot.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by tuckerkirven on 10/11/15.
 */
public class DemoHelper {
    String entry1 = "Dear Diary!\n" +
            "Today has been awful. I felt sick and was not happy and full of energy as usual. I had no phone with me and i don't know if it was that or I just was tired of my whole existence. Well...it was a shitty monday. I had a test in Society. I did study yesterday and it felt okay. For once I actually sat there until the lesson was over and it was about two hours.\n" +
            "\n" +
            "Nothing more did happened...not funny thought. When we ate lunch, Hanna said I was sluggish. Then after I was just pissed. The subject wasn't important, but I feel so freaking angry when people gotta point it out just because i didn't get what she meant first. But I acted like I didn't care about the comment. It was fish in school, yum...\n" +
            "\n" +
            "Now i'm home and i'm writing...I've got to study for my math finals tomorrow. I have to pass the math class because i don't want to study the same shit next year. I just can't do that. Also i did think a lot about my ex. I just can't help it...i think of the good times and i also have a dirty mind and when you're bored the mind can take you to imaginations...however, i also think what he's up to. If he is seeing somebody else...well, actually i don't care. He was an asshole sometimes and so was i. But that's a history i can tell if i'm bored. Now i have to study.";

    String entry2 = "Dear Diary\n" +
            "I just came home from school, it was pretty boring. Did the math test for 4 hours and it didn't felt good, haha. Maybe people think i'm retarded because math is pretty easy right? Nah. Not for me. And i had a awful past with the worst teachers who couldn't teach out right. Trust me, they made me hate math. But i really hope i pass the test, but i don't think so. But heey, at least i tried.\n" +
            "\n" +
            "Nothing more interesting happened in school as usual. I talked with my friends about the prom. It's 2 years left for me. And we have like 2 cute guys in school...not even that, haha. No, but they all are cute and funny in their way i guess. Just not my type. A intelligent, funny and tall guy would be great but they are all short in my school and have no brain *:(* No, i'm not all serious haha...\n" +
            "\n" +
            "At the moment i'm listen to SoMo - Ride. I am really in love with his voice. It's sooo sick, y'll should listen! I usually don't listen to this sensual songs because i just feel lonely and sad because i'm alone and that's sad, but i've learn things and it's awesome to be alone. Did i just wrote that? These past weeks i've felt sad because i'm all by my self. I just finished a relationship. And yeah, i dumped him and i'm not going wrote something bad like all guys are shitty and so on. Holy no. Guys are awesome, even he was that until i realise i was all blind in love. But you learn by your mistakes. As well, i just wanna tell you it's freaking good to be alone and express yourself by your own. You learn new stuff about yourself, at least i do. I see myself with a better look and i found that confidence i didn't had before and learn that people come and go all the time.\n" +
            "\n" +
            "\n" +
            "Yeah...i should get start with my 2 essays but i really don't feel like doing it. I think i'm going watch a tv-show instead. Maybe ex on the beatch. The Swedish version of it. I usual don't like that type of tv-shows but i've tried other things and it's kinda okay. So yup.\n" +
            "\n" +
            "Hope y'll had a great day. I didn't but i'm about to change that now. ";

    String entryNeutralShort = "this is supposed to be a neutral enty. ";
    String entryNeutralLong = "This is supposed to be a neutral entry with a little bit more length. These are just words with no meaning or significant sentiment. ";

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
    public DemoHelper(int numEntries, int avgTimeSecBetween, Activity activity) {
        this.activity = activity;

        List entries = Arrays.asList(a, b, c, d, e, f);

        long nowSec = new Date().getTime()/1000;
        List fakeEntries = new ArrayList();
        List dates = new ArrayList();
        Random rand = new Random();
        for (int i = 0; i < numEntries; i++){
            int randomNum = rand.nextInt(6);
            String entry = "";
            for (int j = 0; j < randomNum; j++){
                int randSent = rand.nextInt((entries.size()));
                entry += entries.get(randSent);
            }
            //fakeEntries.add(entry);

            long nextTime = nowSec - rand.nextInt(avgTimeSecBetween)-avgTimeSecBetween/2;
            System.out.println("nextTime: " + nextTime);
            //dates.add(nextTime);
            nowSec = nextTime;

            process(entry,nextTime);
        }


    }

    public void process(String entryText, long dateSec){
        final Entry entry = new Entry(new Date(dateSec*1000), entryText);

        InfoExtractor ie = new InfoExtractor(activity);

        Uri uri = ie.putEntry(entry);
        String[] segments = uri.getPath().split("/");
        String idStr = segments[segments.length-1];

        RetrieveNLPdata nlp = new RetrieveNLPdata(idStr, entry.getBody());
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

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder;
                InputSource is;

                while ((line = rd.readLine()) != null) {
                    xml += line;
                }

                final ProcessedEntry ent = new ProcessedEntry(xml);


                InfoExtractor ie = new InfoExtractor(activity);
                ie.processNewEntryData(Long.parseLong(entryID), ent);

                final List entries = ie.getAllEntries();

                System.out.println("===================== ENTITIES =======================");



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
