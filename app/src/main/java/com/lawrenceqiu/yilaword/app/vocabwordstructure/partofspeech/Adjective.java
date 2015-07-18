package com.lawrenceqiu.yilaword.app.vocabwordstructure.partofspeech;

import com.lawrenceqiu.yilaword.app.vocabwordstructure.Meaning;

import java.util.ArrayList;

public class Adjective extends PartOfSpeech {

    @Override
    public void addMeaning(Meaning meaning) {
        meaningsList.add(meaning);
    }

    @Override
    public ArrayList<Meaning> getMeaningsList() {
        return meaningsList;
    }

    @Override
    public String toString() {
        return "Adjective";
    }
}
