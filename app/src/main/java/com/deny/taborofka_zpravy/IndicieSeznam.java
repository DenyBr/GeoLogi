package com.deny.taborofka_zpravy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by bruzlzde on 21.3.2018.
 */

public class IndicieSeznam {
    private static IndicieSeznam ourInstance = null;
    public static ArrayList<Indicie> aIndicieVsechny = new ArrayList<Indicie>();
    public static ArrayList<Indicie> aIndicieZiskane = new ArrayList<Indicie>();
    private static Context ctx = null;

    public static IndicieSeznam getInstance(Context context)  {
        if (ourInstance == null) {
            ourInstance = new IndicieSeznam(context);
        }

        ctx = context;
        return ourInstance;
    }

    public static IndicieSeznam getInstance()  {
        //vim, ze tohle muze vratit null, ale z logiky programu bych se tomu mel vyhnout

        return ourInstance;
    }


    public void read (Context context) {
        try {
            aIndicieZiskane = new ArrayList<Indicie>();
            InputStream inputStream =  context.openFileInput(Nastaveni.getInstance(context).getsHra()+Nastaveni.getInstance(context).getiIDOddilu()+"indicieziskane.txt");

            ObjectInputStream in = new ObjectInputStream(inputStream);

            int iPocet = (int) in.readInt();
            //Okynka.zobrazOkynko(this, "Pocet zprav" + iPocet);

            for (int i=0; i<iPocet; i++) {
                Indicie ind = (Indicie) in.readObject();
                aIndicieZiskane.add(ind);
            }
            in.close();
        }
        catch(Exception e) {
            //Okynka.zobrazOkynko(this, "Chyba: " + e.getMessage());
        }

        try {
            aIndicieVsechny = new ArrayList<Indicie>();
            InputStream inputStream =  context.openFileInput( Nastaveni.getInstance(context).getsHra()+Nastaveni.getInstance(context).getiIDOddilu()+"indicievsechny.txt");

            ObjectInputStream in = new ObjectInputStream(inputStream);

            int iPocet = (int) in.readInt();
            //Okynka.zobrazOkynko(this, "Pocet zprav" + iPocet);

            for (int i=0; i<iPocet; i++) {
                Indicie ind = (Indicie) in.readObject();
                aIndicieVsechny.add(ind);
            }
            in.close();
        }
        catch(Exception e) {
            //Okynka.zobrazOkynko(this, "Chyba: " + e.getMessage());
        }
    }

    public void write (Context context) {
        try {
            OutputStream fileOut = context.openFileOutput(Nastaveni.getInstance(context).getsHra()+Nastaveni.getInstance(context).getiIDOddilu()+"indicieziskane.txt", Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeInt(aIndicieZiskane.size());

            for (int i=0; i<aIndicieZiskane.size(); i++) {
                out.writeObject(aIndicieZiskane.get(i));
            }

            out.close();
            fileOut.close();
        } catch (IOException ex) {Okynka.zobrazOkynko(context, "Chyba: " + ex.getMessage());
        }

        try {
            OutputStream fileOut = context.openFileOutput(Nastaveni.getInstance(context).getsHra()+Nastaveni.getInstance(context).getiIDOddilu()+"indicievsechny.txt", Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeInt(aIndicieVsechny.size());

            for (int i=0; i<aIndicieVsechny.size(); i++) {
                out.writeObject(aIndicieVsechny.get(i));
            }

            out.close();
            fileOut.close();
        } catch (IOException ex) {Okynka.zobrazOkynko(context, "Chyba: " + ex.getMessage());
        }
    }

    public void nactizwebu(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            new DownloadWebpageTask(new AsyncResult() {
                @Override
                public void onResult(JSONObject object) {
                    processJson(object);
                }
            }).execute("https://docs.google.com/spreadsheets/d/" + Nastaveni.getInstance(context).getsIdWorkseet() + "/gviz/tq?sheet=Indicie");
        }
        else
        {
            //Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům, seznam platných indicíí nemusí být aktuální");
        }
    }

    private void processJson(JSONObject object) {

        try {
            JSONArray rows = object.getJSONArray("rows");
            IndicieSeznam.getInstance().aIndicieVsechny = new ArrayList<Indicie>();

            for (int r = 1; r < rows.length(); ++r) {
                JSONObject row = rows.getJSONObject(r);
                JSONArray columns = row.getJSONArray("c");

                ArrayList<String> aInd = new ArrayList<String>();
                for (int iSloupec=0; iSloupec<5; iSloupec++) {
                    String sText = "";
                    try {
                        sText = columns.getJSONObject(iSloupec).getString("v");
                        aInd.add(sText);
                    } catch (Exception e) {
                    }
                }

                IndicieSeznam.getInstance().aIndicieVsechny.add(new Indicie(aInd));
            }

            IndicieSeznam.getInstance().write(ctx);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void nactizgdrive(Context context) {
        try {
            authorize(context, "zdenek.bruzl@gmail.com");
        } catch (IOException e) {
            Okynka.zobrazOkynko(context, e.getMessage());

        }

    }

    public void authorize(Context context, String userID) throws IOException {

        Okynka.zobrazOkynko(context, "su tady");
        // load client secrets

        // set up authorization code flow
        Collection<String> scopes = new ArrayList<String>();
        scopes.add(DriveScopes.DRIVE_APPDATA);

        java.io.File dts = context.getCacheDir();

        HttpTransport httpTransport = null;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        DataStoreFactory dataStoreFactory = new FileDataStoreFactory( dts);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory,
                clientSecrets, scopes). setDataStoreFactory(dataStoreFactory).build();
        // authorize
        /*Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()) {
            // Override open browser not working well on Linux and maybe other
            // OS.
            protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws java.io.IOException {

            }
        }.authorize(userID);


        Okynka.zobrazOkynko(this, "inicializovano");

        Drive drive = new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName("taborofka").build();

        Okynka.zobrazOkynko(this, "drive vytvoren");


        String folderId = "19D_IAwk4XNGqGNfaxrPXFKpSulWJydu5";

        File fileMetadata = new File();
        fileMetadata.setName("photo.jpg");
        fileMetadata.setParents(Collections.singletonList(folderId));
        java.io.File filePath = new java.io.File("indicieziskane.txt");
        FileContent mediaContent = new FileContent("txt", filePath);
        File file = drive.files().create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .execute();
        Okynka.zobrazOkynko(this, "File ID: " + file.getId());

*/
    }


    public void nactizftp() {
        connnectingwithFTP("ftp.volny.cz", "bruzl", "ASDKL.");
    }


    public void connnectingwithFTP(String ip, String userName, String pass) {
   /*
        boolean status = false;
        //try {
        try {

            FTPClient mFtpClient = null;

            mFtpClient = new FTPSClient();

            if (mFtpClient!=null) {
                Okynka.zobrazOkynko(this, "jsflksdjflks");

                mFtpClient.setConnectTimeout(10 * 1000);
                mFtpClient.connect(InetAddress.getByName(ip));

                status = mFtpClient.login(userName, pass);

                if (FTPReply.isPositiveCompletion(mFtpClient.getReplyCode())) {
                    mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    mFtpClient.enterLocalPassiveMode();
                    //FTPFile[] mFileArray = mFtpClient.listFiles();
                    FTPFile ftpFile = mFtpClient.mlistFile("tabor.JPG");

                    if (ftpFile != null) {
                        String name = ftpFile.getName();
                        long size = ftpFile.getSize();
                        String timestamp = ftpFile.getTimestamp().getTime().toString();
                        String type = ftpFile.isDirectory() ? "Directory" : "File";

                        Okynka.zobrazOkynko(this, "Name: " + name);
                        System.out.println("Size: " + size);
                        System.out.println("Type: " + type);
                        Okynka.zobrazOkynko(this, "Timestamp: " + timestamp);
                    } else {
                        Okynka.zobrazOkynko(this, "The specified file/directory may not exist!");
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            Okynka.zobrazOkynko(this, "chyba" + e.getMessage());
        }
*/
    }


    public static boolean uzMajiIndicii(String uzMaji) {
        for (int i=0; i<aIndicieZiskane.size(); i++) {
            if (aIndicieZiskane.get(i).jeToOno(uzMaji)) {
                return true;
            }
        }
        return false;
    }

  /*  public ArrayList<String> publikujZiskane() {
        ArrayList<String> a = new ArrayList<String>();

        for (int i=0; i<aIndicieZiskane.size(); i++) {
            a.add(aIndicieZiskane.get(i).getsTexty().get(0));
        }

        return a;
    }
*/

    private IndicieSeznam(Context context) {
        ctx = context;
        read(context);
    }
}
