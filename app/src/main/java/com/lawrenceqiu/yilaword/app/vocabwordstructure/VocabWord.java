package com.lawrenceqiu.yilaword.app.vocabwordstructure;

import com.lawrenceqiu.yilaword.app.vocabwordstructure.partofspeech.PartOfSpeech;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Lawrence on 6/10/2015.
 */
public class VocabWord implements Serializable {
    private int mWordID;
    private String mWord;
    private int mWordLevel;
    private ArrayList<WordTag> mWordTag;
    private ArrayList<PartOfSpeech> mPartOfSpeeches;
    private boolean mIsKnownWord;

    public VocabWord(int mWordID, String mWord, int mWordLevel, ArrayList<WordTag> mWordTag, ArrayList<PartOfSpeech> mPartOfSpeeches) {
        this.mWordID = mWordID;
        this.mWord = mWord;
        this.mWordLevel = mWordLevel;
        this.mWordTag = mWordTag;
        this.mPartOfSpeeches = mPartOfSpeeches;
        this.mIsKnownWord = false;
    }

    public String getWord() {
        return mWord;
    }

    public String getPartOfSpeech() {
        StringBuilder builder = new StringBuilder();
        for (PartOfSpeech speech : mPartOfSpeeches) {
            builder.append(" ");
            builder.append(speech);
            builder.append(" ");
            builder.append("|");
        }
        String partsOfSpeechString = builder.toString();
        return partsOfSpeechString.substring(0, partsOfSpeechString.length() - 1);
    }

    public int getNumberMeanings() {
        int sum = 0;
        for (PartOfSpeech partOfSpeech : mPartOfSpeeches) {
            sum += partOfSpeech.getMeaningsList().size();
        }
        return sum;
    }

    public ArrayList<PartOfSpeech> getPartOfSpeeches() {
        return mPartOfSpeeches;
    }

    public boolean isKnownWord() {
        return mIsKnownWord;
    }

    public void setKnownWord(boolean knownWord) {
        this.mIsKnownWord = knownWord;
    }

    /*
    - No need for these getter methods yet

    public int getWordID() {
        return mWordID;
    }

    public int getWordLevel() {
        return mWordLevel;
    }

    public ArrayList<WordTag> getWordTag() {
        return mWordTag;
    }
    */
}
