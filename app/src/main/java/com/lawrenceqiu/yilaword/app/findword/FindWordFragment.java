package com.lawrenceqiu.yilaword.app.findword;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.lawrenceqiu.yilaword.app.Constants;
import com.lawrenceqiu.yilaword.app.R;
import com.lawrenceqiu.yilaword.app.WebDictionaryActivity;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.Meaning;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.VocabWord;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.partofspeech.PartOfSpeech;

import java.util.ArrayList;

/**
 * Created by Lawrence on 6/29/2015.
 * <p/>
 * Fragment has been prevent from screen rotations. It should prevent some issue from occurring
 * -Disabling screen rotation is not the solution. Must come back to fix this
 */
public class FindWordFragment extends Fragment {
    public final int INITIAL_COUNTDOWN_TIME_MS = 15000;    //15 seconds initially
    public final int COUNTDOWN_INTERVAL = 1000;     //1 second interval
    public final int COUNTDOWN_CORRECT_ANSWER = 5000;     //Correct answer adds 5 seconds
    public final int COUNTDOWN_INCORRECT_ANSWER = 3000;     //Incorrect answer subtracts 3 seconds
    private long mMillisecondsLeft;

    private ArrayList<VocabWord> mVocabWords;
    private ArrayList<VocabWord> mWordOptions;
    private String mCorrectAnswer;
    private CountDownTimer mTimer;

    private TextView mMeaningDisplay;
    private TextView mTimeLeft;
    private Button mFirstButton;
    private Button mSecondButton;
    private Button mThirdButton;
    private Button mFourthButton;
    /**
     * Every time a button is clicked, cancel the timer so a new one can be set up
     * -Correct answer, create a new timer with time + 3 seconds
     * -Incorrect answer, create a new timer with time - 5 seconds
     * Start the time
     */
    private View.OnClickListener handleClickButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mTimer.cancel();                 //Cancel mTimer
            TextView view = (TextView) v;   //Cast to get the text
            if (view.getText().toString().equals(mCorrectAnswer)) {
                Toast.makeText(getActivity(), R.string.correct, Toast.LENGTH_SHORT).show();
                setUpTimer(mMillisecondsLeft + COUNTDOWN_CORRECT_ANSWER);    //Add bonus for correct answer
            } else {
                Toast.makeText(getActivity(), R.string.incorrect, Toast.LENGTH_SHORT).show();
                setUpTimer(mMillisecondsLeft - COUNTDOWN_INCORRECT_ANSWER);  //Subtract for incorrect answer
            }
            loadMeaning(); //Move to next word
            mTimer.start();
        }
    };
    /**
     * Cast the view to a TextView to get the text (the word on the button)
     * Send the word to the AlertDialog and show it
     */
    private View.OnLongClickListener handleLongClickButtonListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            TextView view = (TextView) v;
            final String word = (String) view.getText();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.readInDictionary)
                    .setItems(R.array.dictionaries, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String url = null;
                            switch (which) {
                                case 0:
                                    url = Constants.VOCABULARY_URL + word;
                                    break;
                                case 1:
                                    url = Constants.FREE_DICTIONARY_URL + word;
                                    break;
                                case 2:
                                    url = Constants.MERRIAM_WEBSTER_URL + word;
                                    break;
                                case 3:
                                    url = Constants.REFERENCE_DICTIONARY_URL + word + "?s=t";
                                    break;
                            }
                            Intent loadWeb = new Intent(getActivity(), WebDictionaryActivity.class);
                            loadWeb.putExtra("url", url);
                            startActivity(loadWeb);
                        }
                    });
            builder.create().show();
            return true;
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fagment_find_word, null);
        mMeaningDisplay = (TextView) view.findViewById(R.id.meaningDisplay);
        mTimeLeft = (TextView) view.findViewById(R.id.timeLeft);
        mFirstButton = (Button) view.findViewById(R.id.wordButton1);
        mSecondButton = (Button) view.findViewById(R.id.wordButton2);
        mThirdButton = (Button) view.findViewById(R.id.wordButton3);
        mFourthButton = (Button) view.findViewById(R.id.wordButton4);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Each of these buttons gets the ability for the user long press and get more info
        mFirstButton.setOnClickListener(handleClickButtonListener);
        mFirstButton.setOnLongClickListener(handleLongClickButtonListener);
        mSecondButton.setOnClickListener(handleClickButtonListener);
        mSecondButton.setOnLongClickListener(handleLongClickButtonListener);
        mThirdButton.setOnClickListener(handleClickButtonListener);
        mThirdButton.setOnLongClickListener(handleLongClickButtonListener);
        mFourthButton.setOnClickListener(handleClickButtonListener);
        mFourthButton.setOnLongClickListener(handleLongClickButtonListener);
        loadMeaning();
        setUpTimer(INITIAL_COUNTDOWN_TIME_MS);
    }

    /**
     * Timer only starts when user interacts with the AlertDialog.
     * If user cancels, then it finishes the activity and goes back to the daily list
     * Otherwise, it starts the timer
     */
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.guessWord)
//                Must be format because getString() doesn't display the %n as a newline
                .setMessage(String.format(getString(R.string.findWordInfo)))
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                })
                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mTimer.start();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    /**
     * Create a new mTimer with the initial time and initial countdown interval
     * - (15 sec initial, 1 sec initial)
     *
     * @param initialCountdownTimeMs Number of milliseconds to start with
     */
    private void setUpTimer(long initialCountdownTimeMs) {
        mTimer = new CountDownTimer(initialCountdownTimeMs, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                mMillisecondsLeft = millisUntilFinished;     //Key track of the milliseconds left
                int secondsLeft = (int) (millisUntilFinished / COUNTDOWN_INTERVAL);
                mTimeLeft.setText(String.valueOf(secondsLeft));
                //Based on the time left, the mTimer displays the time in different colors
                if (secondsLeft > 10) {
                    mTimeLeft.setTextColor(getResources().getColor(R.color.fine));
                } else if (secondsLeft > 5) {
                    mTimeLeft.setTextColor(getResources().getColor(R.color.halfWay));
                } else if (secondsLeft > 0) {
                    mTimeLeft.setTextColor(getResources().getColor(R.color.almostOut));
                } else {
                    mTimeLeft.setTextColor(getResources().getColor(R.color.black));
                }
            }

            /**
             * When there is no time left, whether it be mTimer ran out to 0 or user got incorrect answer
             * (then mTimer must be manually set to 0), this method is called
             * <p/>
             * Displays an AlertDialog for the user to decide what to do
             * -Cancel, exit the activity
             * -Otherwise, restart the game with the initial time (15 seconds)
             */
            @Override
            public void onFinish() {
                mTimer.cancel();             //Cancel the mTimer
                mTimeLeft.setText("0");      //In event seconds <= 2 and incorrect answer, it would display 0 as timeleft
                mVocabWords.clear();         //Clear vocabwords, so you can re add the orginal 10 back
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.timeIsUp)
                        .setMessage(R.string.tryAgain)
                        .setCancelable(false)
                        .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                getActivity().finish();
                            }
                        })
                        .setPositiveButton(R.string.restart, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (VocabWord vocabWord : mWordOptions) {
                                    mVocabWords.add(vocabWord);
                                }
                                setUpTimer(INITIAL_COUNTDOWN_TIME_MS);
                                loadMeaning();
                                mTimer.start();
                            }
                        });
                builder.create().show();
            }
        };
    }

    /**
     * Sets up the fragment to display the definition by search through each of the part of speeches
     */
    private void loadMeaning() {
        if (mVocabWords.isEmpty()) {
            noWordsLeft();
            return;
        }
        int randIndex = (int) (mVocabWords.size() * Math.random());
        StringBuilder builder = new StringBuilder();
        VocabWord word = mVocabWords.get(randIndex);
        ArrayList<PartOfSpeech> partOfSpeeches = word.getPartOfSpeeches();
        for (PartOfSpeech partOfSpeech : partOfSpeeches) {
            for (Meaning meaning : partOfSpeech.getMeaningsList()) {
                builder.append(meaning.getMeaning())        //Get the meanings from each part of speech
                        .append("\n");
            }
        }
        int buttonNum = (int) (4 * Math.random()) + 1; //Goes from 1 to 4
        mMeaningDisplay.setText(builder.toString());
        loadWordOptions(word, buttonNum);
    }

    /**
     * Called when no more words are left in the arraylist
     * Displays an AlertDialog to see if the user wants to continue or not
     * -Exit, exits the activity
     * -Otherwise, resets the screen
     */
    private void noWordsLeft() {
        mTimer.cancel();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.noWords)
                .setCancelable(false)
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                })
                .setPositiveButton(R.string.restart, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mTimer != null) {
                            mTimer.cancel();        //Sometimes it may not be set at 0
                        }                           //No words left, timer may not be 0
                        for (VocabWord vocabWord : mWordOptions) {
                            mVocabWords.add(vocabWord);
                        }
                        setUpTimer(INITIAL_COUNTDOWN_TIME_MS);
                        loadMeaning();
                        mTimer.start();
                    }
                });
        builder.create().show();
    }

    /**
     * Loads the options for the meaning of the words. Sets the correct answer
     * and as long as the button is not the correct one, it gets a random word
     *
     * @param word      Of the word in the actual arraylist
     * @param buttonNum Button in which the correct answer is going
     */
    private void loadWordOptions(VocabWord word, int buttonNum) {
        ArrayList<VocabWord> options = new ArrayList<>();       //Must be a copy
        for (VocabWord vocabWord : mWordOptions) {
            options.add(vocabWord);
        }
        options.remove(word);
        mCorrectAnswer = word.getWord();
        int randIndex;
        if (buttonNum != 1) {
            randIndex = (int) (options.size() * Math.random());
            mFirstButton.setText(options.get(randIndex).getWord());
            options.remove(randIndex);
        } else {
            mFirstButton.setText(word.getWord());
        }
        if (buttonNum != 2) {
            randIndex = (int) (options.size() * Math.random());
            mSecondButton.setText(options.get(randIndex).getWord());
            options.remove(randIndex);
        } else {
            mSecondButton.setText(word.getWord());
        }
        if (buttonNum != 3) {
            randIndex = (int) (options.size() * Math.random());
            mThirdButton.setText(options.get(randIndex).getWord());
            options.remove(randIndex);
        } else {
            mThirdButton.setText(word.getWord());
        }
        if (buttonNum != 4) {
            randIndex = (int) (options.size() * Math.random());
            mFourthButton.setText(options.get(randIndex).getWord());
            options.remove(randIndex);
        } else {
            mFourthButton.setText(word.getWord());
        }
        mVocabWords.remove(word);
    }

    /**
     * Loads all the vocab words for the user to try and guess
     *
     * @param vocabWords List of VocabWords
     */
    public void setVocabWords(ArrayList<VocabWord> vocabWords) {
        this.mVocabWords = vocabWords;
        mWordOptions = new ArrayList<>();
        for (VocabWord vocabWord : vocabWords) {
            mWordOptions.add(vocabWord);
        }
    }
}
