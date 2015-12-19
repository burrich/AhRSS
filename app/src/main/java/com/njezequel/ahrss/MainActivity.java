package com.njezequel.ahrss;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.njezequel.ahrss.XmlFeedParser.Entry;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "MaintActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ActionBar init
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Feed static file parsing
        List<Entry> entries = null;
        try {
            XmlFeedParser xmlFeedParser = new XmlFeedParser();
            XmlPullParser parser = getResources().getXml(R.xml.static_feed);

            entries = xmlFeedParser.parse(parser);
        } catch (XmlPullParserException|IOException e) {
            e.printStackTrace();
        }

        // RssListFragment listview init with a SimpleAdapter
        if (entries != null) {
            List<Map<String, String>> data = new ArrayList<>();

            for(Entry entry : entries) {
                Map<String, String> lineData;

                lineData = new HashMap<>();
                lineData.put("title", entry.title);
                lineData.put("date", entry.date);
                data.add(lineData);
            }

            // keys-view mapping of listview line elements
            String[] viewsKeys = {"title", "date"};
            int[] views = {R.id.text1_rss_list, R.id.text2_rss_list};

            // Set adapter
            SimpleAdapter rssListAdapter = new SimpleAdapter(
                    this, data, R.layout.item_rss_list, viewsKeys, views
            );
            ListFragment rssListFragment = (ListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.rss_list_fragment);

            rssListFragment.setListAdapter(rssListAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
