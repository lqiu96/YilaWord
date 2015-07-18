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
    private long millisecondsLeft;

    private ArrayList<VocabWord> vocabWords;
    private ArrayList<VocabWord> wordOptions;
    private String correctAnswer;
    private CountDownTimer timer;

    private TextView meaningDisplay;
    private TextView timeLeft;
    private Button firstButton;
    private Button secondButton;
    private Button thirdButton;
    private Button fourthButton;
    /**
     * Every time a button is clicked, cancel the timer so a new one can be set up
     * -Correct answer, create a new timer with time + 3 seconds
     * -Incorrect answer, create a new timer with time - 5 seconds
     * Start the time
     */
    private View.OnClickListener handleClickButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            timer.cancel();                 //Cancel timer
            TextView view = (TextView) v;   //Cast to get the text
            if (view.getText().toString().equals(correctAnswer)) {
                Toast.makeText(getActivity(), R.string.correct, Toast.LENGTH_SHORT).show();
                setUpTimer(millisecondsLeft + COUNTDOWN_CORRECT_ANSWER);    //Add bonus for correct answer
            } else {
                Toast.makeText(getActivity(), R.string.incorrect, Toast.LENGTH_SHORT).show();
                setUpTimer(millisecondsLeft - COUNTDOWN_INCORRECT_ANSWER);  //Subtract for incorrect answer
            }
            loadMeaning(); //Move to next word
            timer.start();
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
        meaningDisplay = (TextView) view.findViewById(R.id.meaningDisplay);
        timeLeft = (TextView) view.findViewById(R.id.timeLeft);
        firstButton = (Button) view.findViewById(R.id.wordButton1);
        secondButton = (Button) view.findViewById(R.id.wordButton2);
        thirdButton = (Button) view.findViewById(R.id.wordButton3);
        fourthButton = (Button) view.findViewById(R.id.wordButton4);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Each of these buttons gets the ability for the user long press and get more info
        firstButton.setOnClickListener(handleClickButtonListener);
        firstButton.setOnLongClickListener(handleLongClickButtonListener);
        secondButton.setOnClickListener(handleClickButtonListener);
        secondButton.setOnLongClickListener(handleLongClickButtonListener);
        thirdButton.setOnClickListener(handleClickButtonListener);
        thirdButton.setOnLongClickListener(handleLongClickButtonListener);
        fourthButton.setOnClickListener(handleClickButtonListener);
        fourthButton.setOnLongClickListener(handleLongClickButtonListener);
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
                        timer.start();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * Create a new timer with the initial time and initial countdown interval
     * - (15 sec initial, 1 sec initial)
     *
     * @param initialCountdownTimeMs Number of milliseconds to start with
     */
    private void setUpTimer(long initialCountdownTimeMs) {
        timer = new CountDownTimer(initialCountdownTimeMs, COUNTDOWN_INTERVAL) {

            @Override
            public void onTick(long millisUntilFinished) {
                millisecondsLeft = millisUntilFinished;     //Key track of the milliseconds left
                int secondsLeft = (int) (millisUntilFinished / COUNTDOWN_INTERVAL);
                timeLeft.setText(String.valueOf(secondsLeft));
                //Based on the time left, the timer displays the time in different colors
                if (secondsLeft > 10) {
                    timeLeft.setTextColor(getResources().getColor(R.color.fine));
                } else if (secondsLeft > 5) {
                    timeLeft.setTextColor(getResources().getColor(R.color.halfWay));
                } else if (secondsLeft > 0) {
                    timeLeft.setTextColor(getResources().getColor(R.color.almostOut));
                } else {
                    timeLeft.setTextColor(getResources().getColor(R.color.black));
                }
            }

            /**
             * When there is no time left, whether it be timer ran out to 0 or user got incorrect answer
             * (then timer must be manually set to 0), this method is called
             * <p/>
             * Displays an AlertDialog for the user to decide what to do
             * -Cancel, exit the activity
             * -Otherwise, restart the game with the initial time (15 seconds)
             */
            @Override
            public void onFinish() {
                timer.cancel();             //Cancel the timer
                timeLeft.setText("0");      //In event seconds <= 2 and incorrect answer, it would display 0 as timeleft
                vocabWords.clear();         //Clear vocabwords, so you can re add the orginal 10 back
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
                                for (VocabWord vocabWord : wordOptions) {
                                    vocabWords.add(vocabWord);
                                }
                                setUpTimer(INITIAL_COUNTDOWN_TIME_MS);
                                loadMeaning();
                                timer.start();
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
        if (vocabWords.isEmpty()) {
            noWordsLeft();
            return;
        }
        int randIndex = (int) (vocabWords.size() * Math.random());
        StringBuilder builder = new StringBuilder();
        VocabWord word = vocabWords.get(randIndex);
        ArrayList<PartOfSpeech> partOfSpeeches = word.getPartOfSpeeches();
        for (PartOfSpeech partOfSpeech : partOfSpeeches) {
            for (Meaning meaning : partOfSpeech.getMeaningsList()) {
                builder.append(meaning.getMeaning())        //Get the meanings from each part of speech
                        .append("\n");
            }
        }
        int buttonNum = (int) (4 * Math.random()) + 1; //Goes from 1 to 4
        meaningDisplay.setText(builder.toString());
        loadWordOptions(word, buttonNum);
    }

    /**
     * Called when no more words are left in the arraylist
     * Displays an AlertDialog to see if the user wants to continue or not
     * -Exit, exits the activity
     * -Otherwise, resets the screen
     */
    private void noWordsLeft() {
        timer.cancel();
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
                        for (VocabWord vocabWord : wordOptions) {
                            vocabWords.add(vocabWord);
                        }
                        setUpTimer(INITIAL_COUNTDOWN_TIME_MS);
                        loadMeaning();
                        timer.start();
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
        for (VocabWord vocabWord : wordOptions) {
            options.add(vocabWord);
        }
        options.remove(word);
        correctAnswer = word.getWord();
        int randIndex;
        if (buttonNum != 1) {
            randIndex = (int) (options.size() * Math.random());
            firstButton.setText(options.get(randIndex).getWord());
            options.remove(randIndex);
        } else {
            firstButton.setText(word.getWord());
        }
        if (buttonNum != 2) {
            randIndex = (int) (options.size() * Math.random());
            secondButton.setText(options.get(randIndex).getWord());
            options.remove(randIndex);
        } else {
            secondButton.setText(word.getWord());
        }
        if (buttonNum != 3) {
            randIndex = (int) (options.size() * Math.random());
            thirdButton.setText(options.get(randIndex).getWord());
            options.remove(randIndex);
        } else {
            thirdButton.setText(word.getWord());
        }
        if (buttonNum != 4) {
            randIndex = (int) (options.size() * Math.random());
            fourthButton.setText(options.get(randIndex).getWord());
            options.remove(randIndex);
        } else {
            fourthButton.setText(word.getWord());
        }
        vocabWords.remove(word);
    }

    /**
     * Loads all the vocab words for the user to try and guess
     *
     * @param vocabWords List of VocabWords
     */
    public void setVocabWords(ArrayList<VocabWord> vocabWords) {
        this.vocabWords = vocabWords;
        wordOptions = new ArrayList<>();
        for (VocabWord vocabWord : vocabWords) {
            wordOptions.add(vocabWord);
        }
    }
}