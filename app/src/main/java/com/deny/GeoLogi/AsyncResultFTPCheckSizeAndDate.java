package com.deny.GeoLogi;


/**
 * Inspired by kstanoev
 *
 *  Interface pouzivany pro synchronizaci souboru na FTP ulozisti
 */
interface AsyncResultFTPCheckSizeAndDate
{
    void onResult(long lSize, long lTimestamp);
}