package com.deny.GeoLogi;

import org.json.JSONObject;

/**
 * Inspired by kstanoev
 *
 *  Interface pouzivany pro zpracovani json formatu google spreadsheet
 */
interface AsyncResultJSON
{
    void onResult(JSONObject object);
}