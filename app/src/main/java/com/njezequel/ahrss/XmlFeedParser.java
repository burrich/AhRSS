package com.njezequel.ahrss;

import android.text.Html;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide methods in order to parse an xml feed stream (rss or atom).
 * XmlPullParser implementation with Xml.newPullParser().
 * TODO: abastract class
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

    /**
     * Parse the feed which must contains a feed (atom) or channel (rss) tag.
     *
     * @param parser parser object
     * @return List<Entry> ArrayList of all feed entries
     * @throws XmlPullParserException
     * @throws IOException
     */
    private List<Entry> readFeed(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        List<Entry> entries = new ArrayList<>();
        String currentTag = parser.getName();
        String feedTitle = null;

        // Check if it is an rss or atom feed
        if (currentTag.equals("rss")) {
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, ns, "channel");
        } else {
            parser.require(XmlPullParser.START_TAG, ns, "feed");
        }

        // Parsing the feed
        // entry tag is for an atom feed, item for rss
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            currentTag = parser.getName();
            switch (currentTag) {
                case "title":
                    feedTitle = readTitle(parser);
                    break;
                case "entry":
                case "item":
                    Entry entry = readEntry(parser, currentTag);

                    // Setting feed name for the entry
                    entry.setFeed(feedTitle);
                    entries.add(entry);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

        return entries;
    }

    /**
     * Read an entry (or item for rss) tag content.
     *
     * @param parser parser object
     * @param entryTag entry tag name : entry for atom, item for rss
     * @return Entry object
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Entry readEntry(XmlPullParser parser, String entryTag) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, entryTag);

        String title = null;
        String link = null;
        String summary = null;
        String date = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();
            switch (tagName) {
                case "title":
                    title = readTitle(parser);
                    break;
                case "link":
                    link = readLink(parser);
                    break;
                case "description":
                case "summary":
                    summary = readSummary(parser, tagName);
                    break;
                case "content":
                    if (summary == null) {
                        summary = readSummary(parser, tagName);
                        break;
                    }
                case "pubDate":
                case "updated":
                    date = readDate(parser, tagName);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

     return new Entry(title, link, summary, date);
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
     * Read a link tag text content inside a feed tag.
     * Could be a link without attribute or a link with rel and href attribute
     *
     * @param parser parser object
     * @return link string
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");

        String link = "";
        String tag = parser.getName();
        String relType = parser.getAttributeValue(null, "rel");

        if (tag.equals("link")) {
            if (relType == null) {
                link = readText(parser);

            } else {
                if (relType.equals("alternate")){
                    link = parser.getAttributeValue(null, "href");
                }
                parser.nextTag();
            }
        }

        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    /**
     * Read a summary tag text content inside a feed tag.
     *
     * @param parser parser object
     * @param summaryTag description for rss, summary or content for atom
     * @return summary string
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readSummary(XmlPullParser parser, String summaryTag)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, summaryTag);
        String summary = readText(parser);

        if (summaryTag.equals("content")) {
            // TODO: replace img
            summary = Html.fromHtml(summary).toString();
        }

        // Cut text if necessary and concat "..."
        int maxChar = 200;
        if (summary.length() > maxChar) {
            summary = summary.substring(0, maxChar-4).concat("...");
        }

        parser.require(XmlPullParser.END_TAG, ns, summaryTag);
        return summary;
    }

    /**
     * Read a date tag text content inside a feed tag.
     *
     * @param parser parser object
     * @param dateTag pubDate for rss, updated for atom
     * @return date String
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readDate(XmlPullParser parser, String dateTag)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, dateTag);
        String date = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, dateTag);

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
     * Represents a feed element (entry tag for atom, item tag for rss)
     * An instance can be called outside XmlFeedParser class.
     * TODO: remname Entry ? (Item ?)
     */
    public static class Entry {
        public final String title;
        public final String link;
        public final String summary;
        public final String date;
        public String feed;

        /**
         * Default constructor
         *
         * @param title entry title
         * @param link entry link
         * @param summary entry summary
         * @param date entry date
         */
        private Entry(String title, String link, String summary, String date) {
            this.title = title;
            this.link = link;
            this.summary = summary;
            this.date = date;
        }

        /**
         * feed setter
         * @param feed feed name
         */
        private void setFeed(String feed) {
            this.feed = feed;
        }

    }
}
