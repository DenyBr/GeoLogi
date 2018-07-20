package com.deny.GeoLogi;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


import android.content.Context;
import android.util.Log;


/**
 * Created by bruzlzde on 24.2.2018.
 */

public class Nastaveni extends Properties {
    private final static String TAG = "Nastaveni";
    private static Nastaveni instance = null;


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
    private Nastaveni(Context context) {
        reload(context);
    }


    public void reload (Context context) {
        try {
            File file = context.getFileStreamPath("config.properties");

            if(file != null && file.exists()) {
               // Okynka.zobrazOkynko(context, "Jo");
               InputStream inputStream =  context.openFileInput("config.properties");

               load(inputStream);

                //Okynka.zobrazOkynko(context, sHra);

                inputStream.close();

            }
        }
        catch (Exception e) { //ignore error, file will be created later if it does not exist}
        }
    }


    public int getiIDOddilu() {return Integer.parseInt(getProperty("ID", ""));}

    public boolean getisRoot() {return Boolean.parseBoolean(getProperty("Root", ""));}

    public boolean getisSimulation() {boolean res = Boolean.parseBoolean(getProperty("Simulation", ""));
    return res;}

    public boolean getisVerejna() {return Boolean.parseBoolean(getProperty("Verejna", ""));}

    public String getsIdHry() {return getProperty("IDHra", "");}

    public String getsHra() {return getProperty("Hra", "");}

    public String getsNastenka() {return getProperty("Nastenka", "");}

    public String getsIdWorkseet() {return getProperty("IdWorkseet", "");
    }

    public void store (Context context) {
        try {
            OutputStream outputStream = context.openFileOutput("config.properties", Context.MODE_PRIVATE);
            Nastaveni.getInstance().store(outputStream, "");
            outputStream.flush();
            outputStream.close();

            reload(context);
        } catch (Exception e) {
            Log.d(TAG, "Chyba pri zapisu konfigurace : " + e.getMessage());
        }
    }
}
