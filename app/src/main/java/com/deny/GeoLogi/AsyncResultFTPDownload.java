package com.deny.GeoLogi;


/**
 * Inspired by kstanoev
 *
 *  Interface pouzivany pro synchronizaci souboru na FTP ulozisti
 */
interface AsyncResultFTPDownload
{
    //returns
    // 1 ... success
    // 0 ... file not found
    // -1 ... failure => most probably connection failure
    void onResult(int iResult);
}