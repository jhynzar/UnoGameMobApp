package com.example.mj.unogamemobapp;

import java.io.Serializable;

/**
 * Created by Mj on 3/16/2017.
 */

public class Card implements Serializable{
    private int card_color;
    private int card_number; //0 if action card
    private int card_type;
    private String card_name;

    public Card(int _color,int _number,int _type , String _name){
        card_color = _color;
        card_number = _number;
        card_type = _type;
        card_name = _name;
    }
    public String getName(){
        return card_name;
    }

    @Override
    public String toString() {
        return card_name;
    }
    //TODO action

    public String toStringHidden(){return "-------";}


    public boolean isPlayable(int cardBeforeColor, int cardBeforeNumber){
        if(!CardConstants.isActionCard(card_type)){ //normal card
            if(cardBeforeColor == card_color || cardBeforeNumber == card_number){
                return true;
            }
        }else{//action card
            if(card_color == CardConstants.COLOR_ALL){ // if this Card is all
                return true;
            }else if(card_color == cardBeforeColor){ //card before == card in question
                return true;
            }else if(cardBeforeColor == CardConstants.COLOR_ALL){ //card before is all
                return true;
            }
        }
        return false;
    }

    public int getColor() {
        return card_color;
    }

    public int getNumber() {
        return card_number;
    }

    public int getType() {
        return card_type;
    }
}
