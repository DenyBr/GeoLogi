package com.deny.GeoLogi;

interface OverWriter<T> {
    boolean bOverWrite(T member);
    void overwrite (T member);
    boolean bEquals(T member);
    T copy();
}
