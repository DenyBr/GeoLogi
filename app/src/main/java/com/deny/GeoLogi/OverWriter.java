package com.deny.GeoLogi;

interface OverWriter<T> {
    public boolean bOverWrite(T member);
    public void overwrite (T member);
    public boolean bEquals(T member);
    public T copy();
}
