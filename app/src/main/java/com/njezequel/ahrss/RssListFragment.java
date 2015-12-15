package com.njezequel.ahrss;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RssListFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rss_list, container, false);
    }

    /**
     * Génère des données tests afin d'initialiser la listview
     * @return List<Map<String, String>> Liste de dictionnaires représentant chacun une ligne
     */
    public static List<Map<String, String>> generateData() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> lineData;

        lineData = new HashMap<>();
        lineData.put("1", "first line");
        lineData.put("2", "test1");
        data.add(lineData);

        lineData = new HashMap<>();
        lineData.put("1", "second line");
        lineData.put("2", "test2");
        data.add(lineData);

        lineData = new HashMap<>();
        lineData.put("1", "third line");
        lineData.put("2", "test3");
        data.add(lineData);

        return data;
    }
}
