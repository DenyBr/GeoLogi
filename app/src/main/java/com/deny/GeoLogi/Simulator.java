package com.deny.GeoLogi;

import android.content.Context;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Simulator {

    public static void next(Context ctx, ArrayList<Zprava> zpravyZobraz, ArrayList<Zprava> zpravyKomplet) {
        Zprava z = findMsgToShow(ctx, zpravyZobraz, zpravyKomplet);

        if (z!=null) {
            StringBuffer s = new StringBuffer("");

            s.append(checkTime(ctx, z, zpravyKomplet));
            s.append(checkHints(ctx, z));
            s.append(checkLocation(ctx, z));
        }

        Okynka.zobrazOkynko(ctx, "Další zpráva pro vybraného uživatele nebyla nalezena");
    }


    private static Zprava findMsgToShow(Context ctx, ArrayList<Zprava> zpravyZobraz, ArrayList<Zprava> zpravyKomplet) {
         if (zpravyZobraz.size()>0) {
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
        }

        return null;
    }



    private static String checkHints (Context ctx, Zprava z) {
        StringBuffer sRes = new StringBuffer("Přidané indicie: ");

        if (!z.getsPovinneIndicie().equals("")) {
            List<String> items = Arrays.asList(z.getsPovinneIndicie().split("[\\\\s,]+"));

            for (int i=0; i<items.size(); i++) {
                String hint = items.get(i);
                if (!IndicieSeznam.getInstance().uzMajiIndicii(hint)) {
                    if (IndicieSeznam.getInstance().addHint(hint)) {
                        sRes.append(" "+hint);
                    }
                    else {
                        Okynka.zobrazOkynko(ctx, "Povinna indicie "+hint+" neexistuje!");
                    }
                }
            }
        }
        if ((z.getiPocetIndicii()>IndicieSeznam.indiciiZeSkupiny(z.getsIndicieZeSkupiny())))  {
            //some hints from a group has to be added
            int iMissing = IndicieSeznam.indiciiZeSkupiny(z.getsIndicieZeSkupiny()) - z.getiPocetIndicii();

            for (int i=0; i<iMissing; i++) {
                String s=IndicieSeznam.getInstance(ctx).simAddOneOfGroup(z.getsIndicieZeSkupiny());

                if (!s.equals("")) {
                    sRes.append(s);
                 } else {
                    Okynka.zobrazOkynko(ctx, "Neexistuje dostatek indicii ze skupiny "+z.getsIndicieZeSkupiny());
                }
            }
        }


        return "";
    }

    private static String checkTime (Context ctx , Zprava z, ArrayList<Zprava> zpravyKomplet) {
        if (!z.getsZobrazitPoCase().equals("")) {
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
                        Timestamp ts = new Timestamp(zList.getTsCasZobrazeni().getTime()+t.getTime()-t0.getTime());

                        Global.setlTime(ts.getTime());
                        return "Cas nastaven na: " + ts.toString() + "\n";
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
