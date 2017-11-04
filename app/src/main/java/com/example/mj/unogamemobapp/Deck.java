package com.example.mj.unogamemobapp;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Mj on 3/16/2017.
 */

public class Deck implements Serializable{

    private ArrayList<Card> deck;
    private ArrayList<Card> graveyard;

    public Deck(){
        deck = new ArrayList<Card>();
        graveyard = new ArrayList<Card>();


        //create and add normal cards
        deck.addAll(createNormalCards(CardConstants.COLOR_BLUE,CardConstants.TYPE_NORMAL));
        deck.addAll(createNormalCards(CardConstants.COLOR_GREEN,CardConstants.TYPE_NORMAL));
        deck.addAll(createNormalCards(CardConstants.COLOR_RED,CardConstants.TYPE_NORMAL));
        deck.addAll(createNormalCards(CardConstants.COLOR_YELLOW,CardConstants.TYPE_NORMAL));

        //create and add action cards
        //draw two
        deck.addAll(createActionCards(CardConstants.COLOR_BLUE,CardConstants.TYPE_DRAW_TWO,2));
        deck.addAll(createActionCards(CardConstants.COLOR_GREEN,CardConstants.TYPE_DRAW_TWO,2));
        deck.addAll(createActionCards(CardConstants.COLOR_RED,CardConstants.TYPE_DRAW_TWO,2));
        deck.addAll(createActionCards(CardConstants.COLOR_YELLOW,CardConstants.TYPE_DRAW_TWO,2));

        //reverse
        deck.addAll(createActionCards(CardConstants.COLOR_BLUE,CardConstants.TYPE_REVERSE,2));
        deck.addAll(createActionCards(CardConstants.COLOR_GREEN,CardConstants.TYPE_REVERSE,2));
        deck.addAll(createActionCards(CardConstants.COLOR_RED,CardConstants.TYPE_REVERSE,2));
        deck.addAll(createActionCards(CardConstants.COLOR_YELLOW,CardConstants.TYPE_REVERSE,2));

        //skip
        deck.addAll(createActionCards(CardConstants.COLOR_BLUE,CardConstants.TYPE_SKIP,2));
        deck.addAll(createActionCards(CardConstants.COLOR_GREEN,CardConstants.TYPE_SKIP,2));
        deck.addAll(createActionCards(CardConstants.COLOR_RED,CardConstants.TYPE_SKIP,2));
        deck.addAll(createActionCards(CardConstants.COLOR_YELLOW,CardConstants.TYPE_SKIP,2));

        //wild
        deck.addAll(createActionCards(CardConstants.COLOR_ALL,CardConstants.TYPE_WILD,4));
        //wild draw four
        deck.addAll(createActionCards(CardConstants.COLOR_ALL,CardConstants.TYPE_WILD_DRAW_FOUR,4));
        //swap
        deck.addAll(createActionCards(CardConstants.COLOR_ALL,CardConstants.TYPE_SWAP,4));
        //graveyard pick
        deck.addAll(createActionCards(CardConstants.COLOR_ALL,CardConstants.TYPE_GRAVEYARD_PICK,4));
        //deal breaker
        deck.addAll(createActionCards(CardConstants.COLOR_ALL,CardConstants.TYPE_DEAL_BREAKER,4));

        Log.d("Deck Creation",""+deck.size());
    }

    private ArrayList<Card> createActionCards(int _color,int _type, int _howMany){
        ArrayList<Card> toReturn = new ArrayList<Card>();
        for(int i = 0; i<_howMany;i++){
            toReturn.add(new Card(_color,0,_type,CardConstants.getColorName(_color)+CardConstants.getTypeName(_type)));
        }

        return toReturn;
    }
    private ArrayList<Card> createNormalCards(int _color,int _type){
        ArrayList<Card> toReturn = new ArrayList<Card>();
        toReturn.add(new Card(_color,0,CardConstants.TYPE_NORMAL,CardConstants.getColorName(_color)+CardConstants.getTypeName(_type)+"0"));
        for(int number=1;number<=9;number++) {
            toReturn.add(new Card(_color, number, CardConstants.TYPE_NORMAL, CardConstants.getColorName(_color)+CardConstants.getTypeName(_type) + String.valueOf(number)));
            toReturn.add(new Card(_color, number, CardConstants.TYPE_NORMAL, CardConstants.getColorName(_color)+CardConstants.getTypeName(_type) + String.valueOf(number)));
        }

        return toReturn;
    }

    //Manipulation methods
    public Card drawCard(){
        Card toReturn;
        //TODO WHAT IF THERE ARE NO MORE CARDS IN THE DECK, r.nextInt(0); will throw exception
        //TODO HANDLE IT, TRY CATCH IN CATCH BLACK EXECUTE END GAME SCENARIO
        Random r = new Random();
        int i = r.nextInt(deck.size());

        //get the random Card
        toReturn = deck.get(i);
        deck.remove(i);//remove in deck

        return toReturn;
    }
    public void playCard(Card playedCard){
        graveyard.add(playedCard);
    }

    public Boolean isDeckEmpty(){
        if(deck.size() == 0){
            return true;
        }
        return false;
    }
    //getters and setters
    public ArrayList<Card> getDeck(){
        return deck;
    }

    public ArrayList<Card> getGraveyard() {return graveyard;}
}
