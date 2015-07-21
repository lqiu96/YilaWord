package com.lawrenceqiu.yilaword.app.vocablist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lawrenceqiu.yilaword.app.Constants;
import com.lawrenceqiu.yilaword.app.R;
import com.lawrenceqiu.yilaword.app.findword.FindWordActivity;
import com.lawrenceqiu.yilaword.app.scrambledword.ScrambledWordActivity;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.Meaning;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.VocabWord;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.WordTag;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.partofspeech.*;
import com.lawrenceqiu.yilaword.app.wordmeaning.WordMeaningFragment;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by Lawrence on 6/10/2015.
 */
public class VocabWordDisplayFragment extends android.support.v4.app.Fragment
        implements VocabWordAdapter.VocabWordAdapterCallback {
    private int mPosition;
    private int mNumWords;
    private boolean mSwitchOnOff;
    private boolean mSignedIn;
    private Switch mKnownWordSwitch;
    private List<VocabWord> mDailyWords;
    private List<VocabWord> mKnownWords;
    private VocabWordAdapter mVocabWordAdapter;
    private RecyclerView mRecyclerView;
    private Button mGames;
    private Button mReview;
    private SwipeRefreshLayout mRefreshLayout;
    /**
     * Loads up an intent to go the ScrambledWordActivity to play the game
     */
    private View.OnClickListener handleGamesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.spellingGames)
                    .setItems(R.array.games, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0: //Scrambled Word option
                                    Intent loadScrambled = new Intent(getActivity(),
                                            ScrambledWordActivity.class);
                                    loadScrambled.putExtra("NumWords", mNumWords);
                                    for (int i = 0; i < mNumWords; i++) {
                                        loadScrambled.putExtra("word" + i, mDailyWords.get(i));
                                    }
                                    startActivity(loadScrambled);
                                    break;
                                case 1: //Find the word, given the definition
                                    Intent loadMeaning = new Intent(getActivity(),
                                            FindWordActivity.class);
                                    loadMeaning.putExtra("NumWords", mNumWords);
                                    for (int i = 0; i < mNumWords; i++) {
                                        loadMeaning.putExtra("word" + i, mDailyWords.get(i));
                                    }
                                    Log.i("numWords", String.valueOf(mNumWords));
                                    startActivity(loadMeaning);
                                    break;
                                default:
                                    Log.i("Error", "Non of the options chosen");
                            }
                        }
                    });
            builder.create().show();
        }
    };

    private View.OnClickListener handleReviewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MainActivity activity = (MainActivity) getActivity();
            activity.loadReviewWords();
        }
    };
    /**
     * Switches the boolean that determines if the switch is on or off
     * Updates the Recyleview to show either Daily Words or known words based on the switch
     */
    private CompoundButton.OnCheckedChangeListener handleCheckedWordListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mSwitchOnOff = isChecked;
                    displayWordsBaseOnSwitch();
                }
            };

    private SharedPreferences.OnSharedPreferenceChangeListener handlePreferenceChanged =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals(Constants.PREFERENCE_LOGIN)) {
                        mSignedIn = sharedPreferences.getBoolean(Constants.PREFERENCE_LOGIN, false);
                        mNumWords = mSignedIn ? 10 : 5;
                        mKnownWords.clear();                 //Don't know if clearing them and redoing mDailyWords is good
                        mDailyWords = getWordsList();        //Subject to change
                        mVocabWordAdapter = new VocabWordAdapter(getActivity(), mDailyWords, VocabWordDisplayFragment.this);
                        mRecyclerView.setAdapter(mVocabWordAdapter);
                        mKnownWordSwitch.setChecked(false);
                        mSwitchOnOff = false;
                    }
                }
            };
    private SwipeRefreshLayout.OnRefreshListener handleRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            boolean newWords = getWordsFromServer();
            if (newWords) {
                savePreviousDaysWords();        //Save previous days' words list every time
                getDailyWordsList();            //App gets new daily words list
                mDailyWords = getWordsList();
                mKnownWords = new ArrayList<>();
                mVocabWordAdapter = new VocabWordAdapter(getActivity(), mDailyWords, VocabWordDisplayFragment.this);
                mVocabWordAdapter.notifyDataSetChanged();
            }
            mRefreshLayout.setRefreshing(false);
        }
    };

    /**
     * If switch is on, it adds all the known words that the user selected to be displayed
     * If switch is off, displays all the daily words
     * Each displayed screen is first sorted
     */
    private void displayWordsBaseOnSwitch() {
        if (mSwitchOnOff) {
            mKnownWords.clear();
            for (VocabWord vocabWord : mDailyWords) {
                if (vocabWord.isKnownWord()) {
                    mKnownWords.add(vocabWord);
                }
            }
            sort(mKnownWords);
            mVocabWordAdapter = new VocabWordAdapter(getActivity(), mKnownWords, this);
            mRecyclerView.setAdapter(mVocabWordAdapter);
        } else {
            sort(mDailyWords);
            mVocabWordAdapter = new VocabWordAdapter(getActivity(), mDailyWords, this);
            mRecyclerView.setAdapter(mVocabWordAdapter);
        }
    }

    /**
     * Sorts the list based on Alphabetic ordering of their words
     *
     * @param list List of VocabWords
     */
    private void sort(List<VocabWord> list) {
        Collections.sort(list, new Comparator<VocabWord>() {
            @Override
            public int compare(VocabWord lhs, VocabWord rhs) {
                return lhs.getWord().compareTo(rhs.getWord());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView", "in VocabWordDisplayFragment");
        View view = inflater.inflate(R.layout.fragment_word_display, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.vocabWordsList);
        mKnownWordSwitch = (Switch) view.findViewById(R.id.knownWordSwitch);
        mGames = (Button) view.findViewById(R.id.gamesButton);
        mReview = (Button) view.findViewById(R.id.reviewButton);
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.wordsSwipeRefreshLayout);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i("onActivityCreated", "in VocabWordDisplayFragment");
        super.onActivityCreated(savedInstanceState);
        mPosition = 0;           //Position of the item selected is initially set to 0
        mSwitchOnOff = false;    //Initially called to be false

        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(Constants.PREFERENCE_FILE, Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(handlePreferenceChanged);
        mSignedIn = sharedPreferences.getBoolean(Constants.PREFERENCE_LOGIN, false);

        mNumWords = mSignedIn ? 10 : 5;

        boolean newWords = getWordsFromServer();
        if (newWords) {
            savePreviousDaysWords();        //Save previous days' words list every time
            getDailyWordsList();            //App gets new daily words list
        }

        mDailyWords = getWordsList();
        mKnownWords = new ArrayList<>();
        mVocabWordAdapter = new VocabWordAdapter(getActivity(), mDailyWords, this);
        mRecyclerView.setAdapter(mVocabWordAdapter);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        //Set click listeners
        mGames.setOnClickListener(handleGamesListener);
        mReview.setOnClickListener(handleReviewListener);
        mKnownWordSwitch.setOnCheckedChangeListener(handleCheckedWordListener);
        mRefreshLayout.setOnRefreshListener(handleRefreshListener);
        mRefreshLayout.setColorSchemeColors(R.color.refreshOne, R.color.refreshTwo, R.color.refreshThree,
                R.color.refreshFour, R.color.refreshFive);
    }

    @Override
    public void onStart() {
        super.onStart();
        mVocabWordAdapter.notifyDataSetChanged();    //Checks if word has been checked as a known word

        displayWordsBaseOnSwitch();                 //Called to refresh list when back to view this activity
    }

    /*
    * When the activity is stopped, the app will automatically write to the daily list file
    * storing any changes made to the list of vocab words (if they have been marked as known or not)
    *
    * If the user is not logged in, (only displaying 5 words), the words will not be saved. Only
    * if the user is signed in, it will write to the file
    *  - Not signing in would override the daily list file to only show 5 words instead of the 10.
    *      It is a cheap way to make sure that all 10 words will show up
    */
    @Override
    public void onStop() {
        Log.i("onStop", "in VocabWordDisplayFragment");
        super.onStop();     //Done because it would overwrite 10 words with 5 words if not signed in
        if (mSignedIn) {     //If user is SignedIn, all 10 words would be overwritten, saving the IndexOutOfBoundsException
            File file = new File(getActivity().getFilesDir() + File.separator + Constants.DAILY_LIST_FILE);
            try {
                ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
                for (VocabWord vocabWord : mDailyWords) {
                    stream.writeObject(vocabWord);
                }
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads two files dealing with Object streams to write objects to a file
     * Every time this function is called, it pulls all the objects stored in the daily list file
     * and writes them to the Review list file
     */
    private void savePreviousDaysWords() {
        File dailyList = new File(getActivity().getFilesDir() + File.separator + Constants.DAILY_LIST_FILE);
        File reviewList = new File(getActivity().getFilesDir() + File.separator + Constants.REVIEW_LIST_FILE);
        if (!reviewList.exists()) {     //First time getting words from server
            try {
                reviewList.createNewFile();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                        new FileOutputStream(reviewList));
                objectOutputStream.writeObject(Constants.FILE_EMPTY);
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        ObjectInputStream fromDailyList = null;
        ObjectOutputStream toReviewList = null;
        try {
            fromDailyList = new ObjectInputStream(new FileInputStream(dailyList));
            toReviewList = new ObjectOutputStream(new FileOutputStream(reviewList));
            toReviewList.writeObject(Constants.FILE_FULL);
            for (int i = 0; i < 10; i++) {      //10 because you write every object back to the review list
                VocabWord vocabWord = (VocabWord) fromDailyList.readObject();
                toReviewList.writeObject(vocabWord);
                toReviewList.reset();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fromDailyList != null) {
                    fromDailyList.close();
                }
                if (toReviewList != null) {
                    toReviewList.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks the file to read the date that the app got the daily list from the server
     * If the file doesn't exist (firs time opening up app), creates the file and returns true
     * Otherwise, compares the current date with the date in the file
     * -If they are equal returns false (no need to get list from server)
     * -Otherwise, return true (get daily word list from server)
     *
     * @return false is the dates are the same, or true if dates are different or it is first time retreiving words
     */
    private boolean getWordsFromServer() {
        File file = new File(getActivity().getFilesDir() + File.separator + Constants.DAILY_LIST_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;    //If new file, get words from server
        }
        Date today = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        String currentDate = formatDate.format(today);
        String date = getActivity().getSharedPreferences(Constants.PREFERENCE_FILE, Context.MODE_PRIVATE)
                .getString(Constants.PREFERENCE_DATE, null);
        return date == null || !date.equals(currentDate); //If first time date == null, and will gate list from server
    }

    /**
     * Gets the response from the server by 'GET' method. Does not do this async, as it blocks until
     * it gets the response back. It then parses through the JSON response to build the words
     * <p/>
     * Records the date that last accessed the words from the server
     */
    private void getDailyWordsList() {
        HttpGet getWord = new HttpGet("userID");  //Get actual userID
        String jsonResponse = null;
        try {
            jsonResponse = getWord.execute().get(); //Get locks the thread so including a ProgressDialog would not show
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        File file = new File(getActivity().getFilesDir() + File.separator + Constants.DAILY_LIST_FILE);
        ObjectOutputStream stream = null;
        try {
            stream = new ObjectOutputStream(new FileOutputStream(file));
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(jsonResponse); //Must be casted to a String
            JsonArray wordArray = jsonElement.getAsJsonArray();       //List of words stored as an array of 10 elements
            for (int i = 0; i < 10; i++) {      //10- reads everything in, but may not display everything
                JsonElement element = wordArray.get(i);
                JsonObject vocabWord = element.getAsJsonObject();
                int wordId = vocabWord.get("id").getAsInt();
                String name = vocabWord.get("name").getAsString();
                int level = vocabWord.get("level").getAsInt();
                JsonArray tagArray = vocabWord.get("tags").getAsJsonArray();
                Iterator<JsonElement> tagIterator = tagArray.iterator();
                ArrayList<WordTag> tagList = new ArrayList<WordTag>();
                while (tagIterator.hasNext()) {
                    switch (tagIterator.next().getAsString()) {
                        case "new":
                            tagList.add(WordTag.NEW);
                            break;
                        case "ignored":
                            tagList.add(WordTag.IGNORED);
                            break;
                    }
                }
                ArrayList<PartOfSpeech> partOfSpeechArrayList = getPartsOfSpeech(vocabWord);
                VocabWord word = new VocabWord(wordId, name, level, tagList, partOfSpeechArrayList);
                stream.writeObject(word);
                stream.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Date today = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        String currentDate = formatDate.format(today);
        SharedPreferences preferences = getActivity().getSharedPreferences(
                Constants.PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREFERENCE_DATE, currentDate);
        editor.apply();
    }

    /**
     * Get the meanings and examples of each of the words stored in the Json Object
     * Check which part of speech the word contains and create classes based on that
     * Add the meaning into the PartOfSpeech class
     *
     * @param word Json Object of each word
     * @return ArrayList of PartsOfSpeech
     */
    private ArrayList<PartOfSpeech> getPartsOfSpeech(JsonObject word) {
        ArrayList<PartOfSpeech> list = new ArrayList<PartOfSpeech>();
        String[] partOfSpeechesFromServer = Constants.SERVER_PART_OF_SPEECH;
        for (String speech : partOfSpeechesFromServer) {
            if (word.get(speech) != null) {
                JsonArray array = word.get(speech).getAsJsonArray();
                JsonObject object = array.get(0).getAsJsonObject();
                String meaning = object.get("meaning").getAsString();
                String example = object.get("example").getAsString();
                PartOfSpeech partOfSpeech;
                switch (speech) {
                    case "n":
                        partOfSpeech = new Noun();
                        break;
                    case "v":
                        partOfSpeech = new Verb();
                        break;
                    case "adj":
                        partOfSpeech = new Adjective();
                        break;
                    case "adv":
                        partOfSpeech = new Adverb();
                        break;
                    case "pro":
                        partOfSpeech = new Pronoun();
                        break;
                    case "pre":
                        partOfSpeech = new Preposition();
                        break;
                    default:  //or "conj"
                        partOfSpeech = new Conjunction();
                }
                partOfSpeech.addMeaning(new Meaning(meaning, example));
                list.add(partOfSpeech);
            }
        }
        return list;
    }

    /**
     * Reads the VocabWords from file once the callback has finished responding
     * Based on whether the user is signed in (10 words) or has not signed in (5 words)
     *
     * @return List of VocabWords
     */
    private List<VocabWord> getWordsList() {
        File file = new File(getActivity().getFilesDir() + File.separator + Constants.DAILY_LIST_FILE);
        List<VocabWord> words = new ArrayList<>();
        ObjectInputStream stream = null;
        try {
            stream = new ObjectInputStream(new FileInputStream(file));
            for (int i = 0; i < mNumWords; i++) {
                words.add((VocabWord) stream.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return words;
    }

    /**
     * Callback method- Gets the mPosition which the user selected to see the vocab word definition
     *
     * @param position Position selected on the vocab word
     */
    @Override
    public void itemSelected(int position) {
        this.mPosition = position;
        MainActivity activity = (MainActivity) getActivity();
        if (mSwitchOnOff) {
            activity.loadMeaning(mKnownWords.get(position));
        } else {
            activity.loadMeaning(mDailyWords.get(position));
        }
    }

    public void setKnownWord(boolean known) {
        if (mSwitchOnOff) {
            mKnownWords.get(mPosition).setKnownWord(known);
        } else {
            mDailyWords.get(mPosition).setKnownWord(known);
        }
        onStop();
    }
}
