package com.deny.GeoLogi;


import java.sql.Timestamp;

/**
 * Inspired by kstanoev
 *
 *  Interface pouzivany pro synchronizaci souboru na FTP ulozisti
 */
interface AsyncResultFTPCheckSizeAndDate
{
    void onResult(String sSizeTime);
}