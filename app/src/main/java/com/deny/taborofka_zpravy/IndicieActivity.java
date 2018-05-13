package com.deny.taborofka_zpravy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;


import com.example.progress.Taborofka.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;


public class IndicieActivity extends ActionBarActivity {
    private ListView listview;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indicie);

        handler.postDelayed(update, 10);
    }


    private Runnable update = new Runnable() {
        @Override
        public void run() {
            IndicieSeznam.getInstance(IndicieActivity.this).read(IndicieActivity.this);
            prekresli();

            IndicieSeznam.getInstance(IndicieActivity.this).nactizwebu(IndicieActivity.this);

            handler.postDelayed(this, 60000);
        }
    };


    public void buttonClickHandler(View view) {

        EditText etIndH = (EditText) findViewById(R.id.etIndH);
        if (!etIndH.getText().toString().equals("")) {

            if (IndicieSeznam.getInstance(IndicieActivity.this).uzMajiIndicii(etIndH.getText().toString())) {
                Okynka.zobrazOkynko(this, "Tuto indicii už máte");
            } else {
                for (int i = 0; i < IndicieSeznam.getInstance(IndicieActivity.this).aIndicieVsechny.size(); i++) {
                    if (IndicieSeznam.getInstance(IndicieActivity.this).aIndicieVsechny.get(i).jeToOno(etIndH.getText().toString())) {
                        IndicieSeznam.getInstance(IndicieActivity.this).aIndicieZiskane.add(IndicieSeznam.getInstance(IndicieActivity.this).aIndicieVsechny.get(i));
                        IndicieSeznam.getInstance(IndicieActivity.this).write(this);
                        prekresli();

                        setResult(RESULT_OK);

                        return;
                    }
                }

                Okynka.zobrazOkynko(this, "Neplatná indicie");
            }
        }
    }

    private void prekresli () {
        setContentView(R.layout.activity_indicie);
        listview = (ListView) findViewById(R.id.listview);

        final IndicieAdapter adapter = new IndicieAdapter(this, R.layout.indicie, IndicieSeznam.getInstance(this).aIndicieZiskane);
        listview.setAdapter(adapter);
    }
}