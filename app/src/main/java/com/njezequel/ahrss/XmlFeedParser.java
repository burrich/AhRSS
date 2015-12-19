package com.njezequel.ahrss;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide methods in order to parse an xml feed stream.
 * XmlPullParser implementation with Xml.newPullParser().
 *
 * @see XmlPullParser
 */
public class XmlFeedParser {
    /**
     * Namespace for XmlPullParser require method.
     */
    private static final String ns = null;

    /**
     * Parse an xml input stream.
     *
     * @param in feed input stream
     * @return List<Entry> ArrayList of all feed entries
     * @throws XmlPullParserException
     * @throws IOException
     */
    public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    // TODO: remove
    public List<Entry> parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.next();
        parser.nextTag();

        return readFeed(parser);
    }

    /**
     * Parse the feed which must contains a feed tag.
     *
     * @param parser parser object
     * @return List<Entry> ArrayList of all feed entries
     * @throws XmlPullParserException
     * @throws IOException
     */
    private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Entry> entries = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("entry")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }

        return entries;
    }

    /**
     * Read an entry tag content.
     *
     * @param parser parser object
     * @return Entry object
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "entry");

        String title = null;
        String date = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            switch (parser.getName()) {
                case "title":
                    title = readTitle(parser);
                    break;
                case "date":
                    date = readDate(parser);
                    break;
                default:
                    skip(parser);
            }
        }

        return new Entry(title, date);
    }

    /**
     * Read a title tag text content inside a feed tag.
     *
     * @param parser parser object
     * @return title String
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");

        return title;
    }

    /**
     * Read a date tag text content inside a feed tag.
     *
     * @param parser parser object
     * @return date String
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "date");
        String date = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "date");

        return date;
    }

    /**
     * Read a text content from a tag
     *
     * @param parser parser object
     * @return text String
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";

        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }

        return result;
    }

    /**
     * Skip tags recursively
     *
     * @param parser parser object
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    /**
     * Entry static nested class.
     * An instance can be called outside XmlFeedParser class.
     */
    public static class Entry {
        public final String title;
        public final String date;

        /**
         * Default constructor
         * @param title entry title
         * @param date entry date
         */
        private Entry(String title, String date) {
            this.title = title;
            this.date = date;
        }
    }
}
