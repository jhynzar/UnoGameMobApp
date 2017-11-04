package com.example.mj.unogamemobapp;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener{


    /*
    +initialize players
    +initialize game
    -GamePLAY
     */

    //----GAME
    private final int PLAYER_1 = 1;
    private final int PLAYER_2 = 2;

    private final int TEXTSIZE_YOUR_TURN = 30;
    private final int TEXTSIZE_NOT_YOUR_TURN = 14;

    private int mPlayer = 0;

    private boolean didP1Play;
    private boolean didP1Oops;

    private boolean didP2Oops;
    private boolean didP2Play;

    Deck mDeck;

    //for me and enemy
    ArrayList<Card> p1Hand;
    ArrayAdapter<Card> p1HandAdapter;

    ArrayList<Card> p2Hand;
    ArrayAdapter<Card> p2HandAdapter;



    //-----View

    //ViewSwitching
    private ViewSwitcher viewSwitcher;
    private final int CONNECTION_VIEW = 0;
    private final int GAME_VIEW = 1;

    private Button btnPlayAs1,btnPlayAs2;
    private Button btnHost,btnConnect,btnDiscover,btnStartGame;
    private Button p1Hoops,p1EndTurn,p2Hoops,p2EndTurn;
    private ImageButton imgBtnDrawCard;
    private ListView p1HandView,p2HandView;
    private TextView p1YourTurn,p2YourTurn;
    private ImageView imgLastCard;



    //---CONNECTION



    private static final String TAG = "MainActivity";
    //Connection
    Connection mConnection;

    //handler
    private Handler mUpdateHandler;

    //Nsd (Network Service Discovery) - broadcasting
    NsdHelper mNsdHelper;


    Boolean mTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //changing screens
        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);

        //initialize Views
        btnPlayAs1 = (Button) findViewById(R.id.btn_PlayAs1);
        btnPlayAs1.setOnClickListener(this);

        btnPlayAs2 = (Button) findViewById(R.id.btn_PlayAs2);
        btnPlayAs2.setOnClickListener(this);

        btnHost = (Button) findViewById(R.id.btn_Host);
        btnHost.setOnClickListener(this);

        btnConnect = (Button) findViewById(R.id.btn_Connect);
        btnConnect.setOnClickListener(this);

        btnDiscover = (Button) findViewById(R.id.btn_Discover);
        btnDiscover.setOnClickListener(this);

        btnStartGame = (Button) findViewById(R.id.btn_StartGame);
        btnStartGame.setOnClickListener(this);

        p1Hoops = (Button) findViewById(R.id.p1_hoops);
        p1Hoops.setOnClickListener(this);

        p1EndTurn = (Button) findViewById(R.id.p1_EndTurn);
        p1EndTurn.setOnClickListener(this);

        p2Hoops = (Button) findViewById(R.id.p2_hoops);
        p2Hoops.setOnClickListener(this);

        p2EndTurn = (Button) findViewById(R.id.p2_EndTurn);
        p2EndTurn.setOnClickListener(this);

        imgBtnDrawCard = (ImageButton) findViewById(R.id.imgBtn_DrawCard);
        imgBtnDrawCard.setOnClickListener(this);
        imgBtnDrawCard.setBackgroundResource(R.drawable.hoopsdef);

        p1YourTurn = (TextView) findViewById(R.id.p1_YourTurn);
        p2YourTurn = (TextView) findViewById(R.id.p2_YourTurn);


        imgLastCard = (ImageView) findViewById(R.id.img_LastCard);
        imgLastCard.setOnClickListener(this);


        //-----LISTVIEW

        //initialize Deck,new deck
        mDeck = new Deck();

        //add card to graveyard, to follow through
        mDeck.getGraveyard().add(mDeck.drawCard());
        resolveLastPlayedCardPicture(mDeck.getGraveyard().get(0));

        //initialize MY HAND and ENEMY HAND
        p1Hand = new ArrayList<Card>(); //p1 hand which is me
        p2Hand = new ArrayList<Card>(); //p2 hand which is enemy

        //draw card for each hand
        for(int i=0;i<7;i++){
            p1Hand.add(mDeck.drawCard());
            p2Hand.add(mDeck.drawCard());
        }

        p1HandView = (ListView) findViewById(R.id.p1_HandView);
        p2HandView = (ListView) findViewById(R.id.p2_HandView);

        p1HandAdapter = new ArrayAdapter<Card>(this,android.R.layout.simple_list_item_1,p1Hand);
        p2HandAdapter = new ArrayAdapter<Card>(this,android.R.layout.simple_list_item_1,p2Hand);

        p1HandView.setAdapter(p1HandAdapter);
        p2HandView.setAdapter(p2HandAdapter);



        //handler updating UI
        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.e(TAG,"HANDLER IN MAIN");
                Deck deck = (Deck) msg.getData().getSerializable("deck");
                ArrayList<Card> p1Hand = (ArrayList<Card>) msg.getData().getSerializable("p1Hand");
                ArrayList<Card> p2Hand = (ArrayList<Card>) msg.getData().getSerializable("p2Hand");
                int turn = msg.getData().getInt("turn");
                boolean didOops = msg.getData().getBoolean("didOops");
                updateUI(deck,p1Hand,p2Hand,turn,didOops);
            }
        };
        //connection
        mConnection = new Connection(mUpdateHandler);

        //Nsd
        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

        //DISABLE ALL
        //p1
        p1EndTurn.setEnabled(false);
        p1Hoops.setEnabled(false);
        p1HandView.setEnabled(false);
        //p2
        p2EndTurn.setEnabled(false);
        p2Hoops.setEnabled(false);
        p2HandView.setEnabled(false);
    }


    public void endTurn(){
        if(mPlayer == PLAYER_1){
            mConnection.sendData(mDeck,p1Hand,p2Hand,PLAYER_2,didP1Oops);
            p1EndTurn.setEnabled(false);
            p1Hoops.setEnabled(false);

            //reset play
            didP1Oops = false;
            didP1Play = false;
        }else{
            mConnection.sendData(mDeck,p1Hand,p2Hand,PLAYER_1,didP2Oops);
            p2EndTurn.setEnabled(false);
            p2Hoops.setEnabled(false);

            didP2Oops = false;
            didP2Play = false;
        }
    }


    //RESOLVE PLAYED CARDS
    public void resolveByP1LastPlayedCard(int lastPlayedCardType){
        switch (lastPlayedCardType){
            case CardConstants.TYPE_NORMAL:
                //do nothing
                break;
            case CardConstants.TYPE_DRAW_TWO:
                p1Hand.add(mDeck.drawCard());
                p1Hand.add(mDeck.drawCard());
                p1HandAdapter.notifyDataSetChanged();
                break;
            case CardConstants.TYPE_REVERSE:
                //do nothing
                break;
            case CardConstants.TYPE_SKIP:
                endTurn();
                break;
            case CardConstants.TYPE_WILD://changed, other player will pick


            case CardConstants.TYPE_WILD_DRAW_FOUR:
                p1Hand.add(mDeck.drawCard());
                p1Hand.add(mDeck.drawCard());
                p1Hand.add(mDeck.drawCard());
                p1Hand.add(mDeck.drawCard());
                p1HandAdapter.notifyDataSetChanged();
                break;
            case CardConstants.TYPE_SWAP:
                ArrayList<Card> temp;
                temp = p1Hand;
                p1Hand = p2Hand;
                p2Hand = temp;
                p1HandAdapter.notifyDataSetChanged();
                p2HandAdapter.notifyDataSetChanged();
                break;
            case CardConstants.TYPE_GRAVEYARD_PICK:
                break;
            case CardConstants.TYPE_DEAL_BREAKER:
                break;
        }
    }
    public void resolveByP2LastPlayedCard(int lastPlayedCardType){
        switch (lastPlayedCardType){
            case CardConstants.TYPE_NORMAL:
                //do nothing
                break;
            case CardConstants.TYPE_DRAW_TWO:
                p2Hand.add(mDeck.drawCard());
                p2Hand.add(mDeck.drawCard());
                p2HandAdapter.notifyDataSetChanged();
                break;
            case CardConstants.TYPE_REVERSE:
                //do nothing
                break;
            case CardConstants.TYPE_SKIP:
                endTurn();
                break;
            case CardConstants.TYPE_WILD://changed, other player will pick


            case CardConstants.TYPE_WILD_DRAW_FOUR:
                p2Hand.add(mDeck.drawCard());
                p2Hand.add(mDeck.drawCard());
                p2Hand.add(mDeck.drawCard());
                p2Hand.add(mDeck.drawCard());
                p2HandAdapter.notifyDataSetChanged();
                break;
            case CardConstants.TYPE_SWAP:
                ArrayList<Card> temp;
                temp = p1Hand;
                p1Hand = p2Hand;
                p2Hand = temp;
                p1HandAdapter.notifyDataSetChanged();
                p2HandAdapter.notifyDataSetChanged();
                break;
            case CardConstants.TYPE_GRAVEYARD_PICK:
                break;
            case CardConstants.TYPE_DEAL_BREAKER:
                break;
        }
    }

    //RESOLVE PICTURE OF LAST PLAYED CARD
    public void resolveLastPlayedCardPicture(Card card){
        switch (card.getType()){
            case CardConstants.TYPE_NORMAL:
                switch (card.getColor()){
                    case CardConstants.COLOR_BLUE:
                        switch (card.getNumber()){
                            case 0:
                                imgLastCard.setBackgroundResource(R.drawable.bluecard0);
                                break;
                            case 1:
                                imgLastCard.setBackgroundResource(R.drawable.bluecard1);
                                break;
                            case 2:
                                imgLastCard.setBackgroundResource(R.drawable.bluecard2);
                                break;
                            case 3:
                                imgLastCard.setBackgroundResource(R.drawable.bluecard3);
                                break;
                            case 4:
                                imgLastCard.setBackgroundResource(R.drawable.bluecard4);
                                break;
                            case 5:
                                imgLastCard.setBackgroundResource(R.drawable.bluecard5);
                                break;
                            case 6:
                                imgLastCard.setBackgroundResource(R.drawable.bluecard6);
                                break;
                            case 7:
                                imgLastCard.setBackgroundResource(R.drawable.bluecard7);
                                break;
                            case 8:
                                imgLastCard.setBackgroundResource(R.drawable.bluecard8);
                                break;
                            case 9:
                                imgLastCard.setBackgroundResource(R.drawable.bluecard9);
                                break;
                        }
                        break;
                    case CardConstants.COLOR_RED:
                        switch (card.getNumber()){
                            case 0:
                                imgLastCard.setBackgroundResource(R.drawable.redcard0);
                                break;
                            case 1:
                                imgLastCard.setBackgroundResource(R.drawable.redcard1);
                                break;
                            case 2:
                                imgLastCard.setBackgroundResource(R.drawable.redcard2);
                                break;
                            case 3:
                                imgLastCard.setBackgroundResource(R.drawable.redcard3);
                                break;
                            case 4:
                                imgLastCard.setBackgroundResource(R.drawable.redcard4);
                                break;
                            case 5:
                                imgLastCard.setBackgroundResource(R.drawable.redcard5);
                                break;
                            case 6:
                                imgLastCard.setBackgroundResource(R.drawable.redcard6);
                                break;
                            case 7:
                                imgLastCard.setBackgroundResource(R.drawable.redcard7);
                                break;
                            case 8:
                                imgLastCard.setBackgroundResource(R.drawable.redcard8);
                                break;
                            case 9:
                                imgLastCard.setBackgroundResource(R.drawable.redcard9);
                                break;
                        }
                        break;
                    case CardConstants.COLOR_YELLOW:
                        switch (card.getNumber()){
                            case 0:
                                imgLastCard.setBackgroundResource(R.drawable.yellowcard0);
                                break;
                            case 1:
                                imgLastCard.setBackgroundResource(R.drawable.yellowcard1);
                                break;
                            case 2:
                                imgLastCard.setBackgroundResource(R.drawable.yellowcard2);
                                break;
                            case 3:
                                imgLastCard.setBackgroundResource(R.drawable.yellowcard3);
                                break;
                            case 4:
                                imgLastCard.setBackgroundResource(R.drawable.yellowcard4);
                                break;
                            case 5:
                                imgLastCard.setBackgroundResource(R.drawable.yellowcard5);
                                break;
                            case 6:
                                imgLastCard.setBackgroundResource(R.drawable.yellowcard6);
                                break;
                            case 7:
                                imgLastCard.setBackgroundResource(R.drawable.yellowcard7);
                                break;
                            case 8:
                                imgLastCard.setBackgroundResource(R.drawable.yellowcard8);
                                break;
                            case 9:
                                imgLastCard.setBackgroundResource(R.drawable.yellowcard9);
                                break;
                        }
                        break;
                    case CardConstants.COLOR_GREEN:
                        switch (card.getNumber()){
                            case 0:
                                imgLastCard.setBackgroundResource(R.drawable.greencard0);
                                break;
                            case 1:
                                imgLastCard.setBackgroundResource(R.drawable.greencard1);
                                break;
                            case 2:
                                imgLastCard.setBackgroundResource(R.drawable.greencard2);
                                break;
                            case 3:
                                imgLastCard.setBackgroundResource(R.drawable.greencard3);
                                break;
                            case 4:
                                imgLastCard.setBackgroundResource(R.drawable.greencard4);
                                break;
                            case 5:
                                imgLastCard.setBackgroundResource(R.drawable.greencard5);
                                break;
                            case 6:
                                imgLastCard.setBackgroundResource(R.drawable.greencard6);
                                break;
                            case 7:
                                imgLastCard.setBackgroundResource(R.drawable.greencard7);
                                break;
                            case 8:
                                imgLastCard.setBackgroundResource(R.drawable.greencard8);
                                break;
                            case 9:
                                imgLastCard.setBackgroundResource(R.drawable.greencard9);
                                break;
                        }
                        break;
                }
                break;
            case CardConstants.TYPE_DRAW_TWO:
                switch (card.getColor()){
                    case CardConstants.COLOR_BLUE:
                        imgLastCard.setBackgroundResource(R.drawable.draw_two_blue);
                        break;
                    case CardConstants.COLOR_RED:
                        imgLastCard.setBackgroundResource(R.drawable.draw_two_red);
                        break;
                    case CardConstants.COLOR_YELLOW:
                        imgLastCard.setBackgroundResource(R.drawable.draw_two_yellow);
                        break;
                    case CardConstants.COLOR_GREEN:
                        imgLastCard.setBackgroundResource(R.drawable.draw_two_green);
                        break;
                }
                break;
            case CardConstants.TYPE_REVERSE:
                switch (card.getColor()){
                    case CardConstants.COLOR_BLUE:
                        imgLastCard.setBackgroundResource(R.drawable.reverse_blue);
                        break;
                    case CardConstants.COLOR_RED:
                        imgLastCard.setBackgroundResource(R.drawable.reverse_red);
                        break;
                    case CardConstants.COLOR_YELLOW:
                        imgLastCard.setBackgroundResource(R.drawable.reverse_yellow);
                        break;
                    case CardConstants.COLOR_GREEN:
                        imgLastCard.setBackgroundResource(R.drawable.reverse_green);
                        break;
                }
                break;
            case CardConstants.TYPE_SKIP:
                switch (card.getColor()){
                    case CardConstants.COLOR_BLUE:
                        imgLastCard.setBackgroundResource(R.drawable.skip_blue);
                        break;
                    case CardConstants.COLOR_RED:
                        imgLastCard.setBackgroundResource(R.drawable.skip_red);
                        break;
                    case CardConstants.COLOR_YELLOW:
                        imgLastCard.setBackgroundResource(R.drawable.skip_yellow);
                        break;
                    case CardConstants.COLOR_GREEN:
                        imgLastCard.setBackgroundResource(R.drawable.skip_green);
                        break;
                }
                break;
            case CardConstants.TYPE_WILD:
                imgLastCard.setBackgroundResource(R.drawable.wild);
                break;
            case CardConstants.TYPE_WILD_DRAW_FOUR:
                imgLastCard.setBackgroundResource(R.drawable.wild_draw_4);
                break;
            case CardConstants.TYPE_SWAP:
                imgLastCard.setBackgroundResource(R.drawable.swap_card);
                break;
            case CardConstants.TYPE_GRAVEYARD_PICK:
                imgLastCard.setBackgroundResource(R.drawable.graveyard);
                break;
            case CardConstants.TYPE_DEAL_BREAKER:
                imgLastCard.setBackgroundResource(R.drawable.deal_breaker);
                break;
            default:
                imgLastCard.setBackgroundResource(R.drawable.hoopsatt);
                break;
        }
    }



    //all onclick events

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_Host:
                if(mConnection.getLocalPort() > -1) {
                    mNsdHelper.registerService(mConnection.getLocalPort());
                    Log.d(TAG,"registered");
                } else {
                    Log.d(TAG, "ServerSocket isn't bound.");
                }
                //----
                break;
            case R.id.btn_Connect:
                NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
                if (service != null) {
                    Log.d(TAG, "Connecting." + service);
                    mConnection.connectToServer(service.getHost(),
                            service.getPort());
                } else {
                    Log.d(TAG, "No service to connect to!");
                }


                //---
                break;
            case R.id.btn_Discover:
                mNsdHelper.discoverServices();
                break;
            case R.id.p1_hoops:
                if(mPlayer == PLAYER_1){
                    didP1Oops = true; //TODO check who oops in endturn
                }else{
                    p1Hand.add(mDeck.drawCard());
                    p1HandAdapter.notifyDataSetChanged();

                    p1Hoops.setEnabled(false); //to prevent excessive penalties
                }
                break;
            case R.id.p2_hoops:
                if(mPlayer == PLAYER_2){
                    didP2Oops = true;
                }else{
                    p1Hand.add(mDeck.drawCard());
                    p2HandAdapter.notifyDataSetChanged();

                    p2Hoops.setEnabled(false); //to prevent excessive penalties
                }
                break;
            case R.id.p1_EndTurn:
                endTurn();
                break;
            case R.id.p2_EndTurn:
                endTurn();
                break;
            case R.id.imgBtn_DrawCard:
                if(mPlayer == PLAYER_1){
                    Toast.makeText(this,"P1 Draw", Toast.LENGTH_SHORT).show();
                    p1Hand.add(mDeck.drawCard());
                    p1HandAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(this,"P2 Draw", Toast.LENGTH_SHORT).show();
                    p2Hand.add(mDeck.drawCard());
                    p2HandAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.btn_PlayAs1:
                Log.e(TAG,"Played as Player 1");
                mPlayer = 1;
                break;
            case R.id.btn_PlayAs2:
                Log.e(TAG,"Played as Player 2");
                mPlayer = 2;
                break;
            case R.id.btn_StartGame:
                initializePlayers();
        }
    }

    public void switchViewTo(int view){
        Log.d("switchViewTo","switchViewTo"+view);
        switch(view){
            case CONNECTION_VIEW:
                viewSwitcher.showPrevious();
                break;
            case GAME_VIEW:
                viewSwitcher.showNext();
                break;
        }
    }

    public void updateUI(Deck recDeck ,ArrayList<Card> _p1Hand,ArrayList<Card> _p2Hand,int turn,boolean didOops){
        p1Hand = _p1Hand;
        p2Hand = _p2Hand;

        p1HandView = (ListView) findViewById(R.id.p1_HandView);
        p2HandView = (ListView) findViewById(R.id.p2_HandView);

        p1HandAdapter = new ArrayAdapter<Card>(this,android.R.layout.simple_list_item_1,p1Hand);
        p2HandAdapter = new ArrayAdapter<Card>(this,android.R.layout.simple_list_item_1,p2Hand);

        p1HandView.setAdapter(p1HandAdapter);
        p2HandView.setAdapter(p2HandAdapter);


        mDeck = recDeck;
        int lastIndex = mDeck.getGraveyard().size() - 1;

        resolveLastPlayedCardPicture(mDeck.getGraveyard().get(lastIndex));
        //resolve
        if((mPlayer == PLAYER_1) && (turn == PLAYER_1)){
            resolveByP1LastPlayedCard(mDeck.getGraveyard().get(lastIndex).getType());
        }else if((mPlayer == PLAYER_2) && (turn == PLAYER_2)){
            resolveByP2LastPlayedCard(mDeck.getGraveyard().get(lastIndex).getType());
        }

        p1HandAdapter.notifyDataSetChanged();
        p2HandAdapter.notifyDataSetChanged();

        updateYourTurnTV(turn);

        updateDidOops(didOops,turn);
    }

    public void updateYourTurnTV(int turn){
        switch(turn){
            case PLAYER_1:
                //update this regardless if what player are you
                p1YourTurn.setTextSize(TEXTSIZE_YOUR_TURN);
                p2YourTurn.setTextSize(TEXTSIZE_NOT_YOUR_TURN);

                if(mPlayer == PLAYER_1){
                    p1Hoops.setEnabled(true);
                    p1EndTurn.setEnabled(true);
                }
                break;
            case PLAYER_2:
                p1YourTurn.setTextSize(TEXTSIZE_NOT_YOUR_TURN);
                p2YourTurn.setTextSize(TEXTSIZE_YOUR_TURN);

                if(mPlayer == PLAYER_2){
                    p2Hoops.setEnabled(true);
                    p2EndTurn.setEnabled(true);
                }
                break;
        }
    }

    public void updateDidOops(boolean didOops,int turn){
        //for penalty
        if(!didOops){ //didn't oops
            if((mPlayer == PLAYER_1) && (turn == PLAYER_1)){ //if im player 1 and its my turn
                if(isOopsApplicable(PLAYER_2)){
                    p2Hoops.setEnabled(true);
                }
                else{
                    p2Hoops.setEnabled(false);
                }
            }else if((mPlayer ==PLAYER_2) && (turn == PLAYER_2)){
                if(isOopsApplicable(PLAYER_1)){
                    p1Hoops.setEnabled(true);
                }
                else{
                    p2Hoops.setEnabled(false);
                }
            }
        }
    }

    public boolean isOopsApplicable(int playerHandToCheck){
        switch (playerHandToCheck){
            case PLAYER_1:
                if(p1Hand.size() == 1){
                    return true;
                }
                break;
            case PLAYER_2:
                if(p2Hand.size() == 1){
                    return true;
                }
                break;
        }
        return false;
    }




    //initialize Players
    public void initializePlayers(){
        Log.d(TAG,"initPlayers");
        //determine if this device is player 1 or not
        if(mPlayer == PLAYER_1){
            Log.d(TAG,"initPLayers: 1");
            //if player 1 disable player 2 buttons
            p1Hoops.setEnabled(true);
            p1EndTurn.setEnabled(true);

            //to enable scroll to your deck
            p1HandView.setEnabled(true);


            //add listener, so you can't click the other 1
            p1HandView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Card pickedCard = p1HandAdapter.getItem(position);

                    Card lastPlayedCard = mDeck.getGraveyard().get(mDeck.getGraveyard().size() - 1); //lastplayedCard

                    if(!didP1Play){
                        if(pickedCard.isPlayable(lastPlayedCard.getColor(),lastPlayedCard.getNumber())){
                            mDeck.playCard(pickedCard);
                            p1HandAdapter.remove(pickedCard);
                            didP1Play = true;
                        }else{
                            Toast.makeText(MainActivity.this,"Can't play that.",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(MainActivity.this,"Already played a Card.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            Log.d(TAG,"initPLayers: 2");

            //to enable scrolling through your handview
            p2HandView.setEnabled(true);

            p2HandView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Card pickedCard = p2HandAdapter.getItem(position);
                    Card lastPlayedCard = mDeck.getGraveyard().get(mDeck.getGraveyard().size() - 1); //lastplayedCard

                    if(!didP2Play){
                        if(pickedCard.isPlayable(lastPlayedCard.getColor(),lastPlayedCard.getNumber())){
                            mDeck.playCard(pickedCard);
                            p2HandAdapter.remove(pickedCard);
                            didP2Play = true;
                        }else{
                            Toast.makeText(MainActivity.this,"Can't play that.",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(MainActivity.this,"Already played a Card.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        switchViewTo(GAME_VIEW);
    }
    //TO PREVENT OVERLOADING DEVICE AND DESTROY ALL THREADS
    @Override
    protected void onPause() {
        if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
    }

    @Override
    protected void onDestroy() {
        mNsdHelper.tearDown();
        mConnection.tearDown();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        mNsdHelper.tearDown();
        mConnection.tearDown();
        super.onStop();
    }
////TO PREVENT OVERLOADING DEVICE AND DESTROY ALL THREADS - END

    //PREVENT KEYS

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK: //PREVENT BACK KEY, SO YOU CAN ENJOOOY YOUR GAAAMEE WITHOUT THINKING OF MISCLICKS
                return false;
        }

        return super.onKeyDown(keyCode, event);
    }
}