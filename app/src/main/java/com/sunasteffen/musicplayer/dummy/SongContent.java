package com.sunasteffen.musicplayer.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class SongContent {
    public static final List<Song> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<Long, Song> ITEM_MAP = new HashMap<>();

    public static void addItem(Song item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    public static class Song {
        public final long id;
        public final String content;
        public String artist;
        public String details;

        public Song(long id, String content, String artist) {
            this.id = id;
            this.content = content;
            this.artist = artist;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
