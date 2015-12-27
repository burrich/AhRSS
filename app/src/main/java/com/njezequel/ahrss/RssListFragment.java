package com.njezequel.ahrss;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.njezequel.ahrss.XmlFeedParser.Entry;

/**
 * ListFragment for displaying feed(s) entries list.
 * Xml feed is load asynchronously inside the nested DownloadFeedTask class (AsyncTask)
 */
public class RssListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String DEBUG_TAG = "RssListFragment";

    private View mCoordinatorView = null; // Root view of the fragment layout
    private SwipeRefreshLayout mSwipeLayout = null;
    private List<Map<String, String>> mData = null;
    private SimpleAdapter mListAdapter = null;
    private List<String> mFeedUrls = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFeedUrls = new ArrayList<>();

        // http://www.metalorgie.com/feed/news
        // http://www.gamekult.com/feeds/actu.html
        // http://www.byzegut.fr/feeds/posts/default
        // http://stackoverflow.com/feeds
        // http://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml
        mFeedUrls.add("http://www.gamekult.com/feeds/actu.html");
        mFeedUrls.add("http://www.byzegut.fr/feeds/posts/default");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mCoordinatorView = inflater.inflate(R.layout.fragment_rss_list, container, false);

        // Loading rss feed
        // We need the root view (mCoordinatorView) before call in order to use Snackbar inside
        this.loadFeed();

        return mCoordinatorView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setting swipe layout listener for pull to refresh
        mSwipeLayout = (SwipeRefreshLayout)
                getActivity().findViewById(R.id.rss_list_swipe_refresh_layout);
        mSwipeLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        // Updating entries when the user pull to refresh
        loadFeed();
    }

    /**
     * Set a SimpleAdapter to the fragment or update adapter data.
     *
     * @param entries returned after xml parsing
     */
    private void bindDataToAdapter(List<Entry> entries) {
        if (mData == null) { // New adapter
            mData = new ArrayList<>();

            addingEntriesToDataMap(entries);

            // Keys-view mapping of listview line elements
            String[] viewsKeys = {"title", "feed", "summary", "date"};
            int[] views = {
                    R.id.rss_list_title,
                    R.id.rss_list_feed,
                    R.id.rss_list_summary,
                    R.id.rss_list_date
            };

            // Set adapter
            mListAdapter = new SimpleAdapter(
                    getContext(), mData, R.layout.item_rss_list, viewsKeys, views
            );
            this.setListAdapter(mListAdapter);

        } else { // Updating data
            mData.clear();
            addingEntriesToDataMap(entries);
            mListAdapter.notifyDataSetChanged();
            mSwipeLayout.setRefreshing(false);
        }
    }

    /**
     * Set new data to the map which represent list items elements
     *
     * @param entries Entry list
     */
    private void addingEntriesToDataMap(List<Entry> entries) {
        for (Entry entry : entries) {
            Map<String, String> lineData;

            lineData = new HashMap<>();
            lineData.put("title", entry.title);
            lineData.put("feed", entry.feed);
            lineData.put("summary", entry.summary);
            lineData.put("link", entry.link);
            lineData.put("date", formatDate(entry.date));

            mData.add(lineData);
        }
    }

    /**
     * Formating an entry date to a twitter string date style.
     * Return examples :
     * "2 min",
     * "23 h",
     * "20 Dec".
     *
     * @param date an Entry date
     * @return date String
     */
    private String formatDate(Date date) { // TODO: move to XmlFeedParser
        // Computing elapsed time
        long dateTime = date.getTime();
        long elapsedTime = System.currentTimeMillis() - dateTime;

        // Affect right date format (twitter style)
        String dateString;
        if (elapsedTime < 1000 * 60 * 60) { // Less than 1h
            int min = (int) elapsedTime / (1000 * 60);

            if (min == 0)
                min = 1;

            dateString = min + " min";
        } else if (elapsedTime <  1000 * 60 * 60 * 24) { // Less than 24h
            int hour = (int) elapsedTime / (1000 * 60 * 60);

            dateString = hour + " h";
        } else { // Another day
            dateString = new SimpleDateFormat("d MMM", Locale.ENGLISH).format(date);
        }

        return dateString;
    }

    /**
     * Call DownloadFeedTask if the device is connected.
     */
    private void loadFeed() {
        ConnectivityManager connMgr = (ConnectivityManager)
               getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Download RSS feeds in a worker thread
            // When finished, setListViewAdapter() is called
            // TODO: parallel async task with executeOnExecutor()
            new DownloadFeedTask().execute(
                    (String[]) mFeedUrls.toArray(new String[mFeedUrls.size()])
            );
        } else {
            // Connection error
            Snackbar.make(
                    mCoordinatorView,
                    R.string.no_connection,
                    Snackbar.LENGTH_LONG
            ).show();
        }
    }

    /**
     * An AsyncTask task to load xml feed(s).
     * TODO: replace by a Loader ? (AsyncTaskLoader)
     */
    private class DownloadFeedTask extends AsyncTask<String, Void, List<Entry>> {
        private static final int READ_TIMEOUT = 10000;
        private static final int CONNECT_TIMEOUT = 15000;

        @Override
        protected List<Entry> doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls);
            } catch (XmlPullParserException|IOException|IllegalArgumentException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Entry> entries) {
            if (entries != null) {
                // Call bindDataToAdapater() UI thread method
                bindDataToAdapter(entries);
            } else {
                Snackbar.make(
                        mCoordinatorView,
                        R.string.feed_loading_error,
                        Snackbar.LENGTH_LONG
                ).show();
            }
        }

        /**
         * Downloads XML and parses it.
         *
         * @param urlsStrings feeds urls
         * @return list of feed entries
         * @throws XmlPullParserException
         * @throws IOException
         */
        private List<Entry> loadXmlFromNetwork(String[] urlsStrings)
                throws XmlPullParserException, IOException {
            XmlFeedParser parser = new XmlFeedParser();
            List<Entry> entries = new ArrayList<>();
            InputStream stream = null;

            for (String url : urlsStrings) {
                try {
                    stream = downloadUrl(url);
                    List<Entry> urlEntries = parser.parse(stream);
                    entries.addAll(urlEntries);
                } finally {
                    // Makes sure that the InputStream is closed after the app is finished using it
                    if (stream != null) {
                        stream.close();
                    }
                }
            }

            // Return entries sorted by Date desc
            Collections.sort(entries, Collections.reverseOrder());
            return entries;
        }

        /**
         * Given a string representation of a URL, sets up a connection and gets an input stream.
         *
         * @param urlString feed url
         * @return the url input stream
         * @throws IOException
         */
        private InputStream downloadUrl(String urlString) throws IOException {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();
            return conn.getInputStream();
        }
    }
}
