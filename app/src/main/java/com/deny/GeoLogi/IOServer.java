package com.deny.GeoLogi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;

import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.apache.commons.net.io.SocketOutputStream;


public class IOServer {
    final String TAG = "IOServer";

    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    Thread serverThread = null;
    private TextView text;
    private int SERVERPORT;

    public static String sTextToSend="";

    Context ctx;


    public IOServer(Context ctx, int SERVERPORT) {
        this.ctx = ctx;
        this.SERVERPORT = SERVERPORT;

        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
    }


    protected void Stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {
        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);

                Log.d(TAG, "CardServer port otevren");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Chyba pri otevirani serveroveho portu: "+e.getMessage());
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();

                    Log.d(TAG, "Spojeni prijato");

                    CommunicationThread commThread = new CommunicationThread(socket);

                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Chyba pri prijimani spojeni: "+e.getMessage());
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private InputStream input;
        private OutputStream out ;


        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.input = this.clientSocket.getInputStream();
                this.out = this.clientSocket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String sOdesilame = sTextToSend;
                    sTextToSend="";

                    if (!sOdesilame.equals("")) {
                        Log.d(TAG, "Odesilame: "+sOdesilame);

                        byte[] writebuffer = sOdesilame.getBytes("ASCII");

                        out.write(writebuffer);
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}