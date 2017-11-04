package com.example.mj.unogamemobapp;

/**
 * Created by Mj on 3/16/2017.
 */

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Connection{

    private final int PLAYER_1 = 1;
    private final int PLAYER_2 = 2;

    private final int WHAT_START_GAME = 1;
    private final int WHAT_IN_GAME = 2;


    private Handler mUpdateHandler;
    private ChatServer mChatServer;
    private ChatClient mChatClient;

    private static final String TAG = "Connection";

    private Socket mSocket;
    private int mPort = -1;

                                        //ADDED THROWS EXCEPTION TODO
    public Connection(Handler handler){
        mUpdateHandler = handler;
        mChatServer = new ChatServer(handler);
    }

    public void tearDown() {
        mChatServer.tearDown();
        mChatClient.tearDown();
    }

    public void connectToServer(InetAddress address, int port) {
        mChatClient = new ChatClient(address, port);
    }

    public void sendData(Deck deck,ArrayList<Card> p1Hand,ArrayList<Card> p2Hand,int turn,boolean didOops) {
        if (mChatClient != null) {
            mChatClient.sendData(deck,p1Hand,p2Hand,turn,didOops);
        }
    }

    public int getLocalPort() {
        return mPort;
    }

    public void setLocalPort(int port) {
        mPort = port;
        Log.d("setLocalPort",""+mPort);
    }


    public synchronized void updateUI(Deck deck,ArrayList<Card> p1Hand,ArrayList<Card> p2Hand,int turn,boolean didOops) {
        Log.e(TAG, "Updating UI Thread");

        Bundle messageBundle = new Bundle();
        messageBundle.putSerializable("deck",deck);
        messageBundle.putSerializable("p1Hand",p1Hand);
        messageBundle.putSerializable("p2Hand",p2Hand);
        messageBundle.putInt("turn",turn);
        messageBundle.putBoolean("didOops",didOops);

        Message message = new Message();
        message.setData(messageBundle);
        message.what = WHAT_IN_GAME;
        mUpdateHandler.sendMessage(message);
        Log.e(TAG,"Updated UI THREAD");

    }

    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }

    private Socket getSocket() {
        return mSocket;
    }

    private class ChatServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;

        public ChatServer(Handler handler) {
            mThread = new Thread(new ServerThread());
            mThread.start();
        }

        public void tearDown() {
            mThread.interrupt();
            try {
                mServerSocket.close();
            } catch (IOException ioe) {
                Log.e(TAG, "Error when closing server socket.");
            }
        }

        class ServerThread implements Runnable {

            @Override
            public void run() {

                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertise it via Nsd.
                    mServerSocket = new ServerSocket(0);
                    setLocalPort(mServerSocket.getLocalPort());

                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "ServerSocket Created, awaiting connection" + mServerSocket.getLocalPort());
                        setSocket(mServerSocket.accept());
                        Log.d(TAG, "Connected.");
                        if (mChatClient == null) {
                            int port = mSocket.getPort();
                            InetAddress address = mSocket.getInetAddress();
                            connectToServer(address, port);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error creating ServerSocket: ", e);
                    e.printStackTrace();
                }
            }
        }
    }

    private class ChatClient {

        private InetAddress mAddress;
        private int PORT;

        private final String CLIENT_TAG = "ChatClient";




        private Thread mSendThread;
        private Thread mRecThread;


        private ObjectOutputStream os;
        private ObjectInputStream is;

        public ChatClient(InetAddress address, int port) {

            Log.d(CLIENT_TAG, "Creating chatClient");
            this.mAddress = address;
            this.PORT = port;

            mRecThread = new Thread(new ReceivingThread());
            mRecThread.start();


        }


        class ReceivingThread implements Runnable {

            @Override
            public void run() {
                //must not be on main thread
            //if socket not initialized, initialize it. (on client side) (the one that will use connect, it is not initialized in the first place)
            try {
                if (getSocket() == null) {
                    setSocket(new Socket(mAddress, PORT));
                    Log.d(CLIENT_TAG, "Client-side socket initialized.");
                } else {
                    Log.d(CLIENT_TAG, "Socket already initialized. skipping!");
                }
            } catch (UnknownHostException e) {
                Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
            } catch (IOException e) {
                Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
            }
//              catch (Exception e){ //I ADDED THIS TO CLOSE RUNNING SERVICES IN CASE OF AN EXCEPTION
//                Log.d(CLIENT_TAG,e.getMessage());
//            }


                /*
                -Declare the ObjectOutputStream and ObjectInputStream
                -ObjectOutputStream must be declared first before the ObjectInputStream
                -They must only be Initialized(new keyword) ONCE on sockets, because otherwise, y'know ERRORS
                 */
                try{
                    os = new ObjectOutputStream(getSocket().getOutputStream());
                    is = new ObjectInputStream(getSocket().getInputStream());
                }catch(Exception e){
                    Log.d(TAG,e.getMessage());
                }

                try {

                    while (!Thread.currentThread().isInterrupted()) {
                        try{
                            Deck deck = (Deck) is.readUnshared();
                            ArrayList<Card> p1Hand = (ArrayList<Card>) is.readUnshared();
                            ArrayList<Card> p2Hand = (ArrayList<Card>) is.readUnshared();
                            int turn = (int) is.readUnshared();
                            boolean didOops = (boolean) is.readUnshared();

                            Log.d(CLIENT_TAG, "THEY sent deck: " + deck.getDeck().size() + " \n graveyard: " + deck.getGraveyard().size() + " \n p1 hand: "+p1Hand.size()+"\n p2 hand:"+p2Hand.size()+"didOops? "+didOops);
                            updateUI(deck,p1Hand,p2Hand,turn,didOops);
                        }catch(Exception e){
                            Log.d(TAG,e.getMessage());
                        }
                    }
                    is.close();
                } catch (IOException e) {
                    Log.e(CLIENT_TAG, "Server loop error: ", e);
                }
            }
        }



        public void tearDown() {
            mRecThread.interrupt();
            try {
                getSocket().close();
            } catch (IOException ioe) {
                Log.e(CLIENT_TAG, "Error when closing server socket.");
            }
        }


        public void sendData(Deck deck,ArrayList<Card> p1Hand,ArrayList<Card> p2Hand,int turn,boolean didOops) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    Log.d(CLIENT_TAG, "Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
                }
                os.writeUnshared(deck);
                os.writeUnshared(p1Hand);
                os.writeUnshared(p2Hand);
                os.writeUnshared(turn);
                os.writeUnshared(didOops);
                os.flush();
                updateUI(deck,p1Hand,p2Hand,turn,didOops);  //--update with what you sent? don't think so,use only for synchronization ,
            } catch (UnknownHostException e) {
                Log.d(CLIENT_TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.d(CLIENT_TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error3", e);
            }
            Log.d(CLIENT_TAG, "ME sent deck: " + deck.getDeck().size() + " \n graveyard: " + deck.getGraveyard().size() + " \n p1 hand: "+p1Hand.size()+"\n p2 hand:"+p2Hand.size()+"didOops? "+didOops);
        }
    }
}