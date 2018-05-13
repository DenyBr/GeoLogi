package com.deny.taborofka_zpravy;

import java.io.InputStream;
import java.util.Properties;


import android.app.Activity;
import android.content.Context;


/**
 * Created by bruzlzde on 24.2.2018.
 */

public class Nastaveni extends Properties {
    private static Nastaveni instance = null;
    private int iIDOddilu;
    private String sHra;
    private String sIdWorkseet;
    private String sNastenka;


    public static Nastaveni getInstance(Context context) {
        if (instance == null)
        {
            // Create the instance
            instance = new Nastaveni(context);
        }

        return instance;
    }

    public static Nastaveni getInstance() {
        //toto by mohlo vratit null, ale verime tomu, ze bude vzdy napred zavolana instanciace

        return instance;
    }

    private Nastaveni() {}


    public void reload (Context context) {
        try {
            InputStream inputStream =  context.openFileInput("config.properties");

            load(inputStream);

            iIDOddilu= Integer.parseInt(getProperty("ID", "0"));
            sHra= getProperty("Hra", "");
            sNastenka= getProperty("Nastenka", "");
            sIdWorkseet = getProperty("IdWorkseet", "");

            inputStream.close();
        }
        catch (Exception e) { //ignore error, file will be created later if it does not exist}
        }
    }

    private Nastaveni(Context context) {
        reload(context);
    }


    public int getiIDOddilu() {return iIDOddilu;}


    public String getsHra() {return sHra;}

    public String getsNastenka() {return sNastenka;}

    public String getsIdWorkseet() {return sIdWorkseet; }
}
