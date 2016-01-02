package com.njezequel.ahrss;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RssDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RssDetailsFragment extends Fragment {
    private static final String DEBUG_TAG = "RssDetailsFragment";
    private static final String ARG_ENTRY_MAP = "entryMap";

    private FragmentActivity mActivity;
    private Map<?, ?> mEntryMap; // No type in order to manage unchecked casts elt by elt

    /**
     * Factory method to create a new instance of this fragment using the provided parameter.
     *
     * @param entryMap a map with Entry elements
     * @return A new instance of fragment RssDetailsFragment
     */
    public static RssDetailsFragment newInstance(Map<String, String> entryMap) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ENTRY_MAP, (HashMap<String, String>) entryMap);

        RssDetailsFragment fragment = new RssDetailsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Getting entry map argument
        Bundle args = getArguments();
        if (args != null) {
            mEntryMap = (HashMap<?, ?>) args.getSerializable(ARG_ENTRY_MAP);
        }

        mActivity = getActivity();

        setHasOptionsMenu(true); // In order to update appbar (back button)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rss_details, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: wrap textviews inside a webview

        // Getting fragment argument (entry map)
        if (mEntryMap != null) {
            TextView title = (TextView) mActivity.findViewById(R.id.rss_details_title);
            title.setText((String) mEntryMap.get("title"));

            TextView feed = (TextView) mActivity.findViewById(R.id.rss_details_feed);
            feed.setText((String) mEntryMap.get("feed"));

            TextView date = (TextView) mActivity.findViewById(R.id.rss_details_date);
            date.setText((String) mEntryMap.get("date"));

            String contentString = (String) mEntryMap.get("content");
            if (contentString == null)
                contentString = (String) mEntryMap.get("summary");
            WebView content = (WebView) mActivity.findViewById(R.id.rss_details_webview);

            content.setBackgroundColor(Color.TRANSPARENT);
            content.loadData(
                contentString,
                "text/html; charset=utf-8",
                "uft-8"
            );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mActivity.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
