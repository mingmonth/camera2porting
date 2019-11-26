package yskim.sample.camera2porting.data;

import android.net.Uri;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

public class LocalDataList {
    /**
     * We use this as a way to compare a Uri to LocalData instances inside a
     * LinkedList. A linked list in indexOf does a other.equals(get(i)).
     */
    private static class UriWrapper {
        private final Uri mUri;

        public UriWrapper(Uri uri) {
            mUri = uri;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof LocalData)) {
                return false;
            }
            return mUri.equals(((LocalData) o).getContentUri());
        }
    }

    private LinkedList<LocalData> mList = new LinkedList<LocalData>();
    private HashMap<Uri, LocalData> mUriMap = new HashMap<Uri, LocalData>();

    public LocalData get(int index) {
        return mList.get(index);
    }

    public LocalData remove(int index) {
        LocalData removedItem = mList.remove(index);
        mUriMap.remove(removedItem);
        return removedItem;
    }

    public LocalData get(Uri uri) {
        return mUriMap.get(uri);
    }

    public void set(int pos, LocalData data) {
        mList.set(pos, data);
        mUriMap.put(data.getContentUri(), data);
    }

    public void add(LocalData data) {
        mList.add(data);
        mUriMap.put(data.getContentUri(), data);
    }

    public void add(int pos, LocalData data) {
        mList.add(pos, data);
        mUriMap.put(data.getContentUri(), data);
    }

    public int size() {
        return mList.size();
    }

    public void sort(Comparator<LocalData> comparator) {
        Collections.sort(mList, comparator);
    }

    /**
     * This implementation routes through to LinkedList.indexOf, so performs in
     * O(n) but has a fast exit path for when the uri is not contained in the
     * list, and immediately returns -1;
     */
    public int indexOf(Uri uri) {
        if (!mUriMap.containsKey(uri)) {
            return -1;
        }
        return mList.indexOf(new UriWrapper(uri));
    }
}

