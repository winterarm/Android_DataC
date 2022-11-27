package com.winter.dataCollector.vo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class OrderedTagMap implements SortedMap<String, TagInfo> {

    @Nullable
    @Override
    public Comparator<? super String> comparator() {
        return null;
    }

    @NonNull
    @Override
    public SortedMap<String, TagInfo> subMap(String fromKey, String toKey) {
        return null;
    }

    @NonNull
    @Override
    public SortedMap<String, TagInfo> headMap(String toKey) {
        return null;
    }

    @NonNull
    @Override
    public SortedMap<String, TagInfo> tailMap(String fromKey) {
        return null;
    }

    @Override
    public String firstKey() {
        return null;
    }

    @Override
    public String lastKey() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return false;
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return false;
    }

    @Nullable
    @Override
    public TagInfo get(@Nullable Object key) {
        return null;
    }

    @Nullable
    @Override
    public TagInfo put(String key, TagInfo value) {
        return null;
    }

    @Nullable
    @Override
    public TagInfo remove(@Nullable Object key) {
        return null;
    }

    @Override
    public void putAll(@NonNull Map<? extends String, ? extends TagInfo> m) {

    }

    @Override
    public void clear() {

    }

    @NonNull
    @Override
    public Set<String> keySet() {
        return null;
    }

    @NonNull
    @Override
    public Collection<TagInfo> values() {
        return null;
    }

    @NonNull
    @Override
    public Set<Entry<String, TagInfo>> entrySet() {
        return null;
    }
}
