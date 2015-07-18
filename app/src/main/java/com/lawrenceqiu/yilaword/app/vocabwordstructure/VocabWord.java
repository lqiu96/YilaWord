package com.lawrenceqiu.yilaword.app.vocabwordstructure;

import com.lawrenceqiu.yilaword.app.vocabwordstructure.partofspeech.PartOfSpeech;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Lawrence on 6/10/2015.
 */
public class VocabWord implements Serializable {
    private int wordID;
    private String word;
    private int wordLevel;
    private ArrayList<WordTag> wordTag;
    private ArrayList<PartOfSpeech> partOfSpeeches;
    private boolean isKnownWord;

    public VocabWord(int wordID, String word, int wordLevel, ArrayList<WordTag> wordTag, ArrayList<PartOfSpeech> partOfSpeeches) {
        this.wordID = wordID;
        this.word = word;
        this.wordLevel = wordLevel;
        this.wordTag = wordTag;
        this.partOfSpeeches = partOfSpeeches;
        this.isKnownWord = false;
    }

    public String getWord() {
        return word;
    }

    public String getPartOfSpeech() {
        StringBuilder builder = new StringBuilder();
        for (PartOfSpeech speech : partOfSpeeches) {
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
        for (PartOfSpeech partOfSpeech : partOfSpeeches) {
            sum += partOfSpeech.getMeaningsList().size();
        }
        return sum;
    }

    public ArrayList<PartOfSpeech> getPartOfSpeeches() {
        return partOfSpeeches;
    }

    public boolean isKnownWord() {
        return isKnownWord;
    }

    public void setKnownWord(boolean knownWord) {
        this.isKnownWord = knownWord;
    }
}
