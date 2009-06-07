package com.yahoo.json;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Iterator;

public class JsonArrayStringIterable implements Iterable<String> {
    private JSONArray array;

    public JsonArrayStringIterable(JSONArray array) {
        this.array = array;
    }

    public Iterator<String> iterator() {
        return new JSONArrayIterator(array);
    }

    private class JSONArrayIterator implements Iterator<String> {
        private int position = 0;
        private JSONArray array;

        public JSONArrayIterator(JSONArray array) {
            this.array = array;
        }

        public boolean hasNext() {
            return array.length() > position;
        }

        public String next() {
            try {
                return array.getString(position++);
            }
            catch(JSONException e) {
                throw new RuntimeException("Can't get array element", e);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("JSONArrayIterator doesn't support remove()");
        }
    }
}