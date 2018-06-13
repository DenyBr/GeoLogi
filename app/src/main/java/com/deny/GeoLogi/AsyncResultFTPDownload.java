package com.deny.GeoLogi;


/**
 * Inspired by kstanoev
 *
 *  Interface pouzivany pro synchronizaci souboru na FTP ulozisti
 */
interface AsyncResultFTPDownload
{
    void onResult(boolean bResult);
}