package com.example.mj.unogamemobapp;

/**
 * Created by Mj on 3/11/2017.
 */

public class CardConstants {

    //card color
    public final static int COLOR_BLUE = 0;
    public final static int COLOR_GREEN = 1;
    public final static int COLOR_RED = 2;
    public final static int COLOR_YELLOW = 3;
    public final static int COLOR_ALL = 4;


    //card action
    public final static int TYPE_NORMAL = 0;
    public final static int TYPE_DRAW_TWO = 1;
    public final static int TYPE_REVERSE = 2; // DELETE
    public final static int TYPE_SKIP = 3;
    public final static int TYPE_WILD = 4;
    public final static int TYPE_WILD_DRAW_FOUR = 5;
    public final static int TYPE_SWAP = 6;
    public final static int TYPE_GRAVEYARD_PICK = 7;
    public final static int TYPE_DEAL_BREAKER = 8;


    public final static boolean isActionCard(int cardType){
        if(cardType > 0){
            return true;
        }
        return false;
    }

    public static String getTypeName(int _type){
        switch(_type){
            case TYPE_NORMAL:
                return "normal";
            case TYPE_DRAW_TWO:
                return "drawtwo";
            case TYPE_REVERSE:
                return "reverse";
            case TYPE_SKIP:
                return "skip";
            case TYPE_WILD:
                return  "wild";
            case TYPE_WILD_DRAW_FOUR:
                return "wilddrawfour";
            case TYPE_SWAP:
                return "swap";
            case TYPE_GRAVEYARD_PICK:
                return "graveyardpick";
            case TYPE_DEAL_BREAKER:
                return "dealbreaker";
            default:
                return "errorType";
        }
    }

    public static String getColorName(int _color){
        switch(_color){
            case COLOR_BLUE:
                return "blue";
            case COLOR_GREEN:
                return "green";
            case COLOR_RED:
                return "red";
            case COLOR_YELLOW:
                return "yellow";
            case COLOR_ALL:
                return "all";
            default:
                return "errorColor";
        }
    }

}
