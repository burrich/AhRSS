package com.njezequel.ahrss;

import android.text.Html;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

// TODO: abastract class

/**
 * Provide methods in order to parse an xml feed stream (rss or atom).
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

    /**
     * Parse the feed which must contains a feed (atom) or channel (rss) tag.
     *
     * @param parser parser object
     * @return List<Entry> ArrayList of all feed entries
     * @throws XmlPullParserException
     * @throws IOException
     */
    private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
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
    private Entry readEntry(XmlPullParser parser, String entryTag)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, entryTag);

        String title = null;
        String link = null;
        String summary = null;
        Date date = null;

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
            summary = summary.substring(0, maxChar - 4).concat("...");
        }

        parser.require(XmlPullParser.END_TAG, ns, summaryTag);
        return summary;
    }

    /**
     * Read a date tag text content inside a feed tag.
     *
     * @param parser parser object
     * @param dateTag pubDate for rss, updated for atom
     * @return Date object
     * @throws IOException
     * @throws XmlPullParserException
     * @throws IllegalArgumentException
     */
    private Date readDate(XmlPullParser parser, String dateTag)
            throws IOException, XmlPullParserException, IllegalArgumentException {
        parser.require(XmlPullParser.START_TAG, ns, dateTag);
        String dateString = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, dateTag);

        // TODO: manage optionnal publication date for atom feed

        // Get Date from String with correct pattern

        // Possible date patterns
        List<String> compatiblesPatterns = new ArrayList<>();
        // rss
        compatiblesPatterns.add("E, d MMM yyyy k:m:s Z");
        // atom
        compatiblesPatterns.add("yyyy-MM-d'T'k:m:sZ");
        compatiblesPatterns.add("yyyy-MM-d'T'k:m:s'Z'"); // stackoverflow
        compatiblesPatterns.add("yyyy-MM-d'T'k:m:s'.'SZ");
        compatiblesPatterns.add("yyyy-MM-d'T'k:m:s'.'SSZ");
        compatiblesPatterns.add("yyyy-MM-d'T'k:m:s'.'SSSZ"); // zegut
        compatiblesPatterns.add("yyyy-MM-d'T'k:m:s'.'S'Z'");
        compatiblesPatterns.add("yyyy-MM-d'T'k:m:s'.'SS'Z'");
        compatiblesPatterns.add("yyyy-MM-d'T'k:m:s'.'SSS'Z'");

        Date date = null;
        int loopCounter = 0;

        // Checking right pattern
        for (String pattern : compatiblesPatterns) {
            loopCounter++;
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);

            // UTC setting for missing time zone inside the pattern ('Z')
            // Default time zone is the system time zone
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Parsing date string into a Date object
            date = dateFormat.parse(dateString, new ParsePosition(0));

            if (date != null)
                // date found
                break;
            if (loopCounter == compatiblesPatterns.size())
                // Pattern doesn't match
                throw new IllegalArgumentException();
        }

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
     * An instance can be called outside XmlFeedParser class (static class).
     * TODO: remname Entry ? (Item ?)
     */
    public static class Entry {
        // TODO: change type and variables names ?
        public final String title;
        public final String link;
        public final String summary;
        public final Date date;
        public String feed;

        /**
         * Default constructor
         *
         * @param title entry title
         * @param link entry link
         * @param summary entry summary
         * @param date entry date
         */
        private Entry(String title, String link, String summary, Date date) {
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
