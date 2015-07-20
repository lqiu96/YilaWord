package com.lawrenceqiu.yilaword.app.scrambledword;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.lawrenceqiu.yilaword.app.R;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.Meaning;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.VocabWord;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.partofspeech.PartOfSpeech;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Lawrence on 6/11/2015.
 */
public class ScrambledWordFragment extends Fragment {
    //TODO: Eventually get around to fixing so it displays the number of correct answers selected
    protected ArrayList<VocabWord> remainingWords;
    protected String scrambledAnswer;
    protected ProgressBar wordProgress;
    private VocabWord[] mVocabWords;
    private TextView mScrambledWord;
    private TextView mScrambledWordDefinition;
    private EditText mEnteredWord;
    private Button mSubmitAnswer;
//    private int mNumberCorrectAnswers;
    private int mNumberQuestions;

    /**
     * Handles when the user hits either return or enter on the keyboard
     * It acts as if the button has been clicked and submits the query
     */
    private TextView.OnEditorActionListener handleEditorListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mSubmitAnswer.performClick();
            }
            return false;
        }
    };

    /**
     * Handles when the user clicks on the button
     * If the answer submitted int he edittext is equal to the answer, then display a Toast
     * informing user of the correct answer and set up the next word
     * Else
     * Display Toast informing user of the incorrect answer
     * At the end, clear the answer the user entered in the edittext
     */
    private View.OnClickListener handleSubmitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String answer = mEnteredWord.getText().toString();
            if (answer.length() == 0) {
                Toast.makeText(getActivity(), R.string.noAnswer, Toast.LENGTH_SHORT).show();
                return;
            }
            if (answer.toLowerCase().equals(scrambledAnswer.toLowerCase())) {
//                mNumberCorrectAnswers++;
//                Log.i("Correct answers", String.valueOf(mNumberCorrectAnswers));
                Toast.makeText(getActivity(), R.string.correct, Toast.LENGTH_SHORT).show();
                wordProgress.setProgress(wordProgress.getProgress() + 1);
                setUpWord();
            } else {
                Toast.makeText(getActivity(), R.string.incorrect, Toast.LENGTH_SHORT).show();
            }
            mEnteredWord.setText("");
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scrambled_word, container, false);
        // Loads all the view from the layout
        wordProgress = (ProgressBar) view.findViewById(R.id.wordProgress);
        mScrambledWord = (TextView) view.findViewById(R.id.scrambledWord);
        mScrambledWordDefinition = (TextView) view.findViewById(R.id.scrambledWordDefinition);
        mEnteredWord = (EditText) view.findViewById(R.id.userAnswer);
        mSubmitAnswer = (Button) view.findViewById(R.id.submitAnswer);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mEnteredWord.setText(savedInstanceState.getString("currentText"));
            mScrambledWord.setText(savedInstanceState.getString("currentWord"));
            mScrambledWordDefinition.setText(savedInstanceState.getString("currentDefinition"));
            wordProgress.setProgress(savedInstanceState.getInt("progress"));
        } else {
//            mNumberCorrectAnswers = 0;
//            Log.i("Correct Answers", String.valueOf(mNumberCorrectAnswers));
            setUpWord();
        }
        wordProgress.setMax(mNumberQuestions);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set up the listeners
        mEnteredWord.setOnEditorActionListener(handleEditorListener);
        mSubmitAnswer.setOnClickListener(handleSubmitListener);
    }

    /**
     * Checks to see that there are still words for the user to unscramble
     * <p/>
     * Randomly picks a Vocabword from the list of available vocabwords
     * 1. Displays the scrambled word
     * 2. Set the answer
     * 3. Display all the meanings that the vocabword has
     */
    protected void setUpWord() {
        if (remainingWords.isEmpty()) {
            noWordsLeft();
        } else {
            int index = (int) (remainingWords.size() * Math.random());
            VocabWord word = remainingWords.get(index);
            scrambledAnswer = word.getWord();
            String scrambled = scrambleWord(scrambledAnswer);
            mScrambledWord.setText(scrambled); //Set it to scrambled word
            String definition = "";
            for (PartOfSpeech partOfSpeech : word.getPartOfSpeeches()) {
                for (Meaning meaning : partOfSpeech.getMeaningsList()) {
                    definition += meaning.getMeaning() + "\n";
                }
            }
            mScrambledWordDefinition.setText(definition);
            remainingWords.remove(index);
        }
    }

    /**
     * Scrambles the word based on the Fisher-Yates shuffle algorithm
     * => For each element (excluding the first and last elements), it will randomly pick an element
     * to swap with the current index
     * Wikipedia: https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
     *
     * @param scrambledAnswer Word to shuffle
     * @return String of the scrambled word (not guaranteed for for each index to hold a completely different letter)
     */
    private String scrambleWord(String scrambledAnswer) {
        char[] word = scrambledAnswer.toCharArray();
        for (int i = word.length - 2; i >= 1; i--) {
            int index = (int) (i * Math.random()) + 1;
            char temp = word[i];
            word[i] = word[index];
            word[index] = temp;
        }
        return new String(word);
    }

    /**
     * Informs the user that there are no more wordsList to be scrambled in the list
     * If the user cancels the dialog, the screen displays that there are no wordsList and disables the button
     * If the user restarts the dialog, all the wordsList and definition are recreated and loaded back into the hashmap
     * -Progress is set to 0 and game must set up again
     */
    private void noWordsLeft() {
//        Log.i("Correct Answer", String.valueOf(mNumberCorrectAnswers));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.empty)
                .setMessage("No words left")
//                .setMessage(getString(R.string.noWordsGiveScore, mNumberCorrectAnswers, mNumberQuestions))
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mScrambledWord.setText(R.string.noWords);
                        mScrambledWordDefinition.setText("");
                        mSubmitAnswer.setEnabled(false);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.restart, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        mNumberCorrectAnswers = 0;
//                        Log.i("Correct answers", String.valueOf(mNumberCorrectAnswers));
                        Collections.addAll(remainingWords, mVocabWords);
                        wordProgress.setProgress(0);
                        if (!mSubmitAnswer.isEnabled()) {
                            mSubmitAnswer.setEnabled(true);
                        }
                        setUpWord();
                    }
                });
        builder.create().show();
    }

    /**
     * When the user rotates the screen, it saves the current word, current definitions to be displayed
     * It stores if the user began to type and answer to be scrambled and the number of wordsList left to solve
     * It stores the correct answer of the scrambled word
     * It also stores each of the remaining wordsList and definitions to solve
     *
     * @param outState Bundle to save the data
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentText", mEnteredWord.getText().toString());
        outState.putString("currentWord", mScrambledWord.getText().toString());
        outState.putString("currentDefinition", mScrambledWordDefinition.getText().toString());
        outState.putInt("progress", wordProgress.getProgress());

    }

    /**
     * Sets the list of vocabwords to be used in the game
     * Creates an arraylist of remainingWords, which (for now, is vocabWords).
     * -Is arrayList so words can easily be removed
     *
     * @param vocabWords Array of VocabWords
     */
    public void setVocabWords(VocabWord[] vocabWords) {
        this.mVocabWords = vocabWords;
        this.mNumberQuestions = this.mVocabWords.length;
        this.remainingWords = new ArrayList<>(mNumberQuestions);
        Collections.addAll(this.remainingWords, vocabWords);
    }
}
