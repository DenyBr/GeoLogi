package com.deny.GeoLogi;

import android.content.Context;
import android.util.Log;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Simulator {
    private static final String TAG ="Simulator";

    public static void next(Context ctx, ArrayList<Zprava> zpravyZobraz, ArrayList<Zprava> zpravyKomplet) {
        Log.d(TAG, "ENTER: next");

        Zprava z = findMsgToShow(ctx, zpravyZobraz, zpravyKomplet);


        if (z!=null) {
            Log.d(TAG, "Message to be shown: " + z.getsPredmet());

            StringBuffer s = new StringBuffer("Zprava, ktera by mela byt zobrazena: ");

            s.append(z.getsPredmet());
            s.append("\n\n");
            s.append(checkHints(ctx, z));
            s.append("\n");
            s.append(checkTime(ctx, z, zpravyKomplet));
            s.append("\n");
            s.append(checkLocation(ctx, z));
            s.append("\n");

            Okynka.zobrazOkynko(ctx, s.toString());
        }
        else {
            Okynka.zobrazOkynko(ctx, "Další zpráva pro vybraného uživatele nebyla nalezena");
        }

        Log.d(TAG, "LEAVE: next");
    }

    private static boolean bIsShown(Zprava z, ArrayList<Zprava> zpravyZobraz) {
        for (int i=0; i<zpravyZobraz.size(); i++) {
            if (z.getiId() == zpravyZobraz.get(i).getiId()) {
             Log.d(TAG, z.getiId() + " is shown");
                return true;
            }
        }
        Log.d(TAG, z.getiId() + " is not shown");
        return false;
    }

    private static Zprava findMsgToShow(Context ctx, ArrayList<Zprava> zpravyZobraz, ArrayList<Zprava> zpravyKomplet) {
        Log.d(TAG, "ENTER: findMsgToShow");
        for (int i=0; i<zpravyKomplet.size(); i++) {
            Zprava z=zpravyKomplet.get(i);

            if (    (!(bIsShown(z, zpravyZobraz))) &&
                    (z.getsNezobrazovatPokudMajiIndicii().equals("") || (!IndicieSeznam.getInstance(ctx).uzMajiIndicii(z.getsNezobrazovatPokudMajiIndicii()))) &&
                    ((z.getiOddil() == 0) || (z.getiOddil() == Nastaveni.getInstance(ctx).getiIDOddilu()))) {
                Log.d(TAG, "LEAVE: findMsgToShow - OK");
                return z;
            }
        }


        /* if (zpravyZobraz.size()>0) {
             //find last shown message
             Zprava lastShown = zpravyZobraz.get(0);
             int iIndexFrom = 0;

             for (int i = 0; i < zpravyKomplet.size(); i++) {
                if (zpravyKomplet.get(i).getiId() == lastShown.getiId()) {
                    iIndexFrom = i + 1;
                    break;
                }
             }

             while (iIndexFrom < zpravyKomplet.size()) {
                Zprava z = zpravyKomplet.get(iIndexFrom);

                //the next message for a given user
                if (((z.getiOddil() == 0) || (z.getiOddil() == Nastaveni.getInstance(ctx).getiIDOddilu())))
                    return z;

                iIndexFrom++;
            }
        }*/

        Log.d(TAG, "LEAVE: findMsgToShow - FAIL - no message found");
        return null;
    }



    private static String checkHints (Context ctx, Zprava z) {
        Log.d(TAG, "ENTER: checkHints");

        StringBuffer sRes = new StringBuffer("");

        if (!z.getsPovinneIndicie().equals("")) {
            List<String> items = Arrays.asList(z.getsPovinneIndicie().split("[\\\\s,]+"));

            for (int i=0; i<items.size(); i++) {
                String hint = items.get(i);
                if (!IndicieSeznam.getInstance().uzMajiIndicii(hint)) {
                    if (IndicieSeznam.getInstance().addHint(hint)) {
                        sRes.append(hint);
                        sRes.append(" ");
                    }
                    else {
                        sRes.append(" Povinna indicie ");
                        sRes.append(hint);
                        sRes.append(" neexistuje! ");
                    }
                }
            }
        }
        if ((z.getiPocetIndicii()>IndicieSeznam.indiciiZeSkupiny(z.getsIndicieZeSkupiny())))  {
            //some hints from a group has to be added
            int iMissing = z.getiPocetIndicii()-IndicieSeznam.indiciiZeSkupiny(z.getsIndicieZeSkupiny());

            for (int i=0; i<iMissing; i++) {
                String s=IndicieSeznam.getInstance(ctx).simAddOneOfGroup(z.getsIndicieZeSkupiny());

                if (!s.equals("")) {
                    sRes.append(s);
                    sRes.append(" ");
                 } else {
                    sRes.append("Neexistuje dostatek indicii ze skupiny ");
                    sRes.append(z.getsIndicieZeSkupiny());
                    break;
                }
            }
        }

        Log.d(TAG, "LEAVE: checkHints");
        if (sRes.toString().equals("")) return "";
                                  else return "Přidané indicie: "+sRes.toString();
    }

    private static String checkTime (Context ctx , Zprava z, ArrayList<Zprava> zpravyKomplet) {
        Log.d(TAG, "ENTER: checkTime : "+z.getsZobrazitPoCase());
        if (z.getsZobrazitPoCase().equals("")) {
            return "";
        } else {
            if (z.getiPoZpraveCislo()==0) {
                //set absolute time
                Timestamp t = Timestamp.valueOf(z.getsZobrazitPoCase());
                t.setTime(t.getTime()+1000);

                Global.setlTime(t.getTime());

                return "Cas nastaven na: " + t.toString() + "\n";
            } else {
                //set relative time
                Time t = Time.valueOf(z.getsZobrazitPoCase());
                Time t0 = Time.valueOf("0:00:00");

                for (int i=0; i<zpravyKomplet.size(); i++) {
                    Zprava zList = zpravyKomplet.get(i);
                    if (zList.getiId()==z.getiPoZpraveCislo()) {
                        if (null == zList.getTsCasZobrazeni()) {
                            return "Cas nelze nastavit, protoze zprava: " + zList.getsPredmet() + " nebyla jeste zobrazena";
                        } else {
                            Timestamp ts = new Timestamp(zList.getTsCasZobrazeni().getTime() + t.getTime() - t0.getTime()+1000);

                            Global.setlTime(ts.getTime());
                            return "Cas nastaven na: " + ts.toString() + "\n";
                        }
                    }
                }
            }

        }
        return "";
    }



        private static String checkLocation (Context ctx , Zprava z) {
        if (z.getfZobrazitNaLat()!=0) {
            Global.setdLat(z.getfZobrazitNaLat());
            Global.setdLong(z.getfZobrazitNaLong());
            GeoBod bodZ =new GeoBod(Global.getLat(), Global.getLong(), "", false);

            GeoBod b = null;
            boolean bFound = false;

            for (int i=0; i<GeoBody.getInstance(ctx).aBody.size(); i++) {
                b = GeoBody.getInstance(ctx).aBody.get(i);
                if (b.bEquals(bodZ)) {
                    bFound = true;
                    break;
                }
             }

            return "GPS souřadnice nastaveny na: " + Global.getLat() + ":"+Global.getLong() + ((bFound)?b.getPopis():""+"\n");
        }
        return "";
    }

}
