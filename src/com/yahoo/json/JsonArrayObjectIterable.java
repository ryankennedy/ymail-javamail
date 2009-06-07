package com.yahoo.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonArrayObjectIterable implements Iterable<JSONObject> {
    private JSONArray array;

    public JsonArrayObjectIterable(JSONArray array) {
        this.array = array;
    }

    public Iterator<JSONObject> iterator() {
        return new JSONArrayIterator(array);
    }

    private class JSONArrayIterator implements Iterator<JSONObject> {
        private int position = 0;
        private JSONArray array;

        public JSONArrayIterator(JSONArray array) {
            this.array = array;
        }

        public boolean hasNext() {
            return array.length() > position;
        }

        public JSONObject next() {
            try {
                return array.getJSONObject(position++);
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