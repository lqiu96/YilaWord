package com.lawrenceqiu.yilaword.app.vocablist;

import android.app.AlertDialog;
import android.app.Fragment;
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
        implements VocabWordAdapter.VocabWordAdapterCallback, WordMeaningFragment.KnownWordCallback {
    private int position;
    private Switch knownWordSwitch;
    private boolean switchOnOff;
    private List<VocabWord> dailyWords;
    private List<VocabWord> knownWords;
    private VocabWordAdapter vocabWordAdapter;
    private RecyclerView recyclerView;
    private Button games;
    private Button review;
    private SwipeRefreshLayout refreshLayout;
    private boolean signedIn;
    private int numWords;
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
                                    loadScrambled.putExtra("numWords", numWords);
                                    for (int i = 0; i < numWords; i++) {
                                        loadScrambled.putExtra("word" + i, dailyWords.get(i));
                                    }
                                    startActivity(loadScrambled);
                                    break;
                                case 1: //Find the word, given the definition
                                    Intent loadMeaning = new Intent(getActivity(),
                                            FindWordActivity.class);
                                    loadMeaning.putExtra("numWords", numWords);
                                    for (int i = 0; i < numWords; i++) {
                                        loadMeaning.putExtra("word" + i, dailyWords.get(i));
                                    }
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
                    switchOnOff = isChecked;
                    displayWordsBaseOnSwitch();
                }
            };

    private SharedPreferences.OnSharedPreferenceChangeListener handlePreferenceChanged =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals(Constants.PREFERENCE_LOGIN)) {
                        signedIn = sharedPreferences.getBoolean(Constants.PREFERENCE_LOGIN, false);
                        numWords = signedIn ? 10 : 5;
                        knownWords.clear();                 //Don't know if clearing them and redoing dailyWords is good
                        dailyWords = getWordsList();        //Subject to change
                        vocabWordAdapter = new VocabWordAdapter(getActivity(), dailyWords, VocabWordDisplayFragment.this);
                        recyclerView.setAdapter(vocabWordAdapter);
                        knownWordSwitch.setChecked(false);
                        switchOnOff = false;
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
                dailyWords = getWordsList();
                knownWords = new ArrayList<>();
                vocabWordAdapter = new VocabWordAdapter(getActivity(), dailyWords, VocabWordDisplayFragment.this);
                vocabWordAdapter.notifyDataSetChanged();
            }
            refreshLayout.setRefreshing(false);
        }
    };

    /**
     * If switch is on, it adds all the known words that the user selected to be displayed
     * If switch is off, displays all the daily words
     * Each displayed screen is first sorted
     */
    private void displayWordsBaseOnSwitch() {
        if (switchOnOff) {
            knownWords.clear();
            for (VocabWord vocabWord : dailyWords) {
                if (vocabWord.isKnownWord()) {
                    knownWords.add(vocabWord);
                }
            }
            sort(knownWords);
            vocabWordAdapter = new VocabWordAdapter(getActivity(), knownWords, this);
            recyclerView.setAdapter(vocabWordAdapter);
        } else {
            sort(dailyWords);
            vocabWordAdapter = new VocabWordAdapter(getActivity(), dailyWords, this);
            recyclerView.setAdapter(vocabWordAdapter);
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
        recyclerView = (RecyclerView) view.findViewById(R.id.vocabWordsList);
        knownWordSwitch = (Switch) view.findViewById(R.id.knownWordSwitch);
        games = (Button) view.findViewById(R.id.gamesButton);
        review = (Button) view.findViewById(R.id.reviewButton);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.wordsSwipeRefreshLayout);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i("onActivityCreated", "in VocabWordDisplayFragment");
        super.onActivityCreated(savedInstanceState);
        position = 0;           //Position of the item selected is initially set to 0
        switchOnOff = false;    //Initially called to be false

        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(Constants.PREFERENCE_FILE, Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(handlePreferenceChanged);
        signedIn = sharedPreferences.getBoolean(Constants.PREFERENCE_LOGIN, false);

        numWords = signedIn ? 10 : 5;

        boolean newWords = getWordsFromServer();
        if (newWords) {
            savePreviousDaysWords();        //Save previous days' words list every time
            getDailyWordsList();            //App gets new daily words list
        }
        dailyWords = getWordsList();
        knownWords = new ArrayList<>();
        vocabWordAdapter = new VocabWordAdapter(getActivity(), dailyWords, this);
        recyclerView.setAdapter(vocabWordAdapter);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        //Set click listeners
        games.setOnClickListener(handleGamesListener);
        review.setOnClickListener(handleReviewListener);
        knownWordSwitch.setOnCheckedChangeListener(handleCheckedWordListener);
        refreshLayout.setOnRefreshListener(handleRefreshListener);
        refreshLayout.setColorSchemeColors(R.color.refreshOne, R.color.refreshTwo, R.color.refreshThree,
                R.color.refreshFour, R.color.refreshFive);
    }

    @Override
    public void onStart() {
        super.onStart();
        vocabWordAdapter.notifyDataSetChanged();    //Checks if word has been checked as a known word

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
        if (signedIn) {     //If user is signedIn, all 10 words would be overwritten, saving the IndexOutOfBoundsException
            File file = new File(getActivity().getFilesDir() + File.separator + Constants.DAILY_LIST_FILE);
            try {
                ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
                for (VocabWord vocabWord : dailyWords) {
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
     * and writes them to the review list file
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
            for (int i = 0; i < 10; i++) {
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
     * Loads the Async Http Client to get the daily list from the server
     * Sets the timeout to 2.5 seconds- If no response after 2.5 seconds, stop
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
            for (int i = 0; i < 10; i++) {
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
            for (int i = 0; i < numWords; i++) {
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

    @Override
    public void itemSelected(int position) {
        this.position = position;
        MainActivity activity = (MainActivity) getActivity();
        if (switchOnOff) {
            activity.loadMeaning(knownWords.get(position));
        } else {
            activity.loadMeaning(dailyWords.get(position));
        }
    }

    @Override
    public void isKnownWord(boolean known) {
        Log.i("Is known", String.valueOf(known));
        if (switchOnOff) {
            knownWords.get(position).setKnownWord(known);
        } else {
            dailyWords.get(position).setKnownWord(known);
        }
        onStop();       //Save changes to file since the fragment.replace in MainActivity
    }                   //Rewrites a new Dailylist from file
}
