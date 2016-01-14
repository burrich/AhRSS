package com.njezequel.ahrss;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RssDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RssDetailsFragment extends Fragment {
    private static final String DEBUG_TAG = "RssDetailsFragment";
    private static final String ARG_ENTRY_STRING = "entryString";

    private MainActivity mActivity;
    private String mEntry;

    /**
     * Factory method to create a new instance of this fragment using the provided parameter.
     *
     * @param entryHtml a map with Entry elements
     * @return A new instance of fragment RssDetailsFragment
     */
    public static RssDetailsFragment newInstance(String entryHtml) {
        Bundle args = new Bundle();
        args.putString(ARG_ENTRY_STRING, entryHtml);

        RssDetailsFragment fragment = new RssDetailsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Getting entry string argument
        Bundle args = getArguments();
        if (args != null) {
            mEntry = args.getString(ARG_ENTRY_STRING);
        }

        mActivity = (MainActivity) getActivity();

        setHasOptionsMenu(true);
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

        if (mEntry != null) {
            WebView content = (WebView) mActivity.findViewById(R.id.rss_details_webview);

            // Load html into webview
            content.loadData(
                    mEntry,
                    "text/html; charset=utf-8",
                    "uft-8"
            );
            content.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_refresh).setVisible(false);
    }
}
