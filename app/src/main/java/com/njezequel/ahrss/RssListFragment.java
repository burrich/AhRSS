package com.njezequel.ahrss;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.njezequel.ahrss.XmlFeedParser.Entry;

/**
 * ListFragment for displaying a feed entries list.
 * Xml feed is load asynchronously inside the nested DownloadFeedTask class (AsyncTask)
 */
public class RssListFragment extends ListFragment {
    private static final String FEED_URL = "http://jezequel-n.com/static_feed.xml";

    /**
     * Root view of the fragment layout.
     */
    private View mCoordinatorView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mCoordinatorView = inflater.inflate(R.layout.fragment_rss_list, container, false);

        // Loading rss feed
        // We need the root view (mCoordinatorView) before call in order to use Snackbar inside
        loadFeed();

        return mCoordinatorView;
    }

    /**
     * Set a SimpleAdapter to the fragment.
     *
     * @param entries Entry list
     */
    private void setListViewAdapter(List<Entry> entries) {
        List<Map<String, String>> data = new ArrayList<>();

        for (Entry entry : entries) {
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
                getContext(), data, R.layout.item_rss_list, viewsKeys, views
        );
        this.setListAdapter(rssListAdapter);
    }

    /**
     * Call DownloadFeedTask if the device is connected.
     */
    private void loadFeed() {
        ConnectivityManager connMgr = (ConnectivityManager)
               getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Download RSS feed in a worker thread
            // When finished, setListViewAdapter() is called
            new DownloadFeedTask().execute(FEED_URL);
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
     * An AsyncTask task to load an xml feed.
     * TODO: replace by a Loader ?
     */
    private class DownloadFeedTask extends AsyncTask<String, Void, List<Entry>> {
        private static final int READ_TIMEOUT = 10000;
        private static final int CONNECT_TIMEOUT = 15000;

        @Override
        protected List<Entry> doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (XmlPullParserException|IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Entry> entries) {
            if (entries != null) {
                setListViewAdapter(entries);
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
         * @param urlString feed url
         * @return list of feed entries
         * @throws XmlPullParserException
         * @throws IOException
         */
        private List<Entry> loadXmlFromNetwork(String urlString)
                throws XmlPullParserException, IOException {
            XmlFeedParser parser = new XmlFeedParser();
            List<Entry> entries = null;
            InputStream stream = null;

            try {
                stream = downloadUrl(urlString);
                entries = parser.parse(stream);
            } finally {
                // Makes sure that the InputStream is closed after the app is finished using it
                if (stream != null) {
                    stream.close();
                }
            }

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
