package com.lawrenceqiu.yilaword.app.vocabwordstructure.partofspeech;

import com.lawrenceqiu.yilaword.app.vocabwordstructure.Meaning;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Lawrence on 6/13/2015.
 */

public abstract class PartOfSpeech implements Serializable {
    public ArrayList<Meaning> meaningsList = new ArrayList<>();
    public abstract void addMeaning(Meaning meaning);
    public abstract ArrayList<Meaning> getMeaningsList();
}
//Unfortunately Enums can't work because each Enum is implicitly static
//So adding a new Meaning to the arraylist for one partofspeech in one word
//adds it to all of them

/**
 * E.g. Not actually correct, just for the sake of an example
 *  Deride has 1 Noun PartOfSpeech and 2 meanings
 *  Test has 1 Noun PartOfSpeech and 1 meaning
 *
 *  Deride should only have 2 meanings
 *  Test should only have 1 meaning      -> Both for Nouns
 *
 *  But they would each have 3 meanings
 */

/*
public enum PartOfSpeech implements Serializable {
    NOUN {
        private ArrayList<Meaning> meaningsList = new ArrayList<>();

        @Override
        public void addMeaning(Meaning meaning) {
            meaningsList.add(meaning);
        }

        @Override
        public ArrayList<Meaning> getMeaningsList() {
            return meaningsList;
        }

        @Override
        public void clearMeanings() {
            meaningsList.clear();
        }
    },
    VERB {
        private ArrayList<Meaning> meaningsList = new ArrayList<>();

        @Override
        public void addMeaning(Meaning meaning) {
            meaningsList.add(meaning);
        }

        @Override
        public ArrayList<Meaning> getMeaningsList() {
            return meaningsList;
        }

        @Override
        public void clearMeanings() {
            meaningsList.clear();
        }
    },
    ADJECTIVE {
        private ArrayList<Meaning> meaningsList = new ArrayList<>();

        @Override
        public void addMeaning(Meaning meaning) {
            meaningsList.add(meaning);
        }

        @Override
        public ArrayList<Meaning> getMeaningsList() {
            return meaningsList;
        }

        @Override
        public void clearMeanings() {
            meaningsList.clear();
        }
    }, ADVERB {
        private ArrayList<Meaning> meaningsList = new ArrayList<>();

        @Override
        public void addMeaning(Meaning meaning) {
            meaningsList.add(meaning);
        }

        @Override
        public ArrayList<Meaning> getMeaningsList() {
            return meaningsList;
        }

        @Override
        public void clearMeanings() {
            meaningsList.clear();
        }
    }, PRONOUN {
        private ArrayList<Meaning> meaningsList = new ArrayList<>();

        @Override
        public void addMeaning(Meaning meaning) {
            meaningsList.add(meaning);
        }

        @Override
        public ArrayList<Meaning> getMeaningsList() {
            return meaningsList;
        }

        @Override
        public void clearMeanings() {
            meaningsList.clear();
        }
    }, PREPOSITION {
        private ArrayList<Meaning> meaningsList = new ArrayList<>();

        @Override
        public void addMeaning(Meaning meaning) {
            meaningsList.add(meaning);
        }

        @Override
        public ArrayList<Meaning> getMeaningsList() {
            return meaningsList;
        }

        @Override
        public void clearMeanings() {
            meaningsList.clear();
        }
    }, CONJUNCTION {
        private ArrayList<Meaning> meaningsList = new ArrayList<>();

        @Override
        public void addMeaning(Meaning meaning) {
            meaningsList.add(meaning);
        }

        @Override
        public ArrayList<Meaning> getMeaningsList() {
            return meaningsList;
        }

        @Override
        public void clearMeanings() {
            meaningsList.clear();
        }
    };

    public abstract void addMeaning(Meaning meaning);
    public abstract ArrayList<Meaning> getMeaningsList();
    public abstract void clearMeanings();
}
*/
