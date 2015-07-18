package com.lawrenceqiu.yilaword.app.scrambledword;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
    protected ArrayList<VocabWord> remainingWords;
    protected String scrambledAnswer;
    protected ProgressBar wordProgress;
    private VocabWord[] vocabWords;
    private TextView scrambledWord;
    private TextView scrambledWordDefinition;
    private EditText enteredWord;
    private Button submitAnswer;

    private int numberCorrectAnswers;
    private int numberQuestions;

    /**
     * Handles when the user hits either return or enter on the keyboard
     * It acts as if the button has been clicked and submits the query
     */
    private TextView.OnEditorActionListener handleEditorListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitAnswer.performClick();
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
            String answer = enteredWord.getText().toString();
            if (answer.length() == 0) {
                Toast.makeText(getActivity(), R.string.noAnswer, Toast.LENGTH_SHORT).show();
                return;
            }
            if (answer.toLowerCase().equals(scrambledAnswer.toLowerCase())) {
                numberCorrectAnswers++;
                Toast.makeText(getActivity(), R.string.correct, Toast.LENGTH_SHORT).show();
                wordProgress.setProgress(wordProgress.getProgress() + 1);
                setUpWord();
            } else {
                Toast.makeText(getActivity(), R.string.incorrect, Toast.LENGTH_SHORT).show();
            }
            enteredWord.setText("");
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scrambled_word, container, false);
        // Loads all the view from the layout
        wordProgress = (ProgressBar) view.findViewById(R.id.wordProgress);
        scrambledWord = (TextView) view.findViewById(R.id.scrambledWord);
        scrambledWordDefinition = (TextView) view.findViewById(R.id.scrambledWordDefinition);
        enteredWord = (EditText) view.findViewById(R.id.userAnswer);
        submitAnswer = (Button) view.findViewById(R.id.submitAnswer);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            enteredWord.setText(savedInstanceState.getString("currentText"));
            scrambledWord.setText(savedInstanceState.getString("currentWord"));
            scrambledWordDefinition.setText(savedInstanceState.getString("currentDefinition"));
            wordProgress.setProgress(savedInstanceState.getInt("progress"));
        } else {
            numberCorrectAnswers = 0;
            setUpWord();
        }
        wordProgress.setMax(numberQuestions);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set up the listeners
        enteredWord.setOnEditorActionListener(handleEditorListener);
        submitAnswer.setOnClickListener(handleSubmitListener);
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
            scrambledWord.setText(scrambled); //Set it to scrambled word
            StringBuilder builder = new StringBuilder();
            for (PartOfSpeech partOfSpeech : word.getPartOfSpeeches()) {
                for (Meaning meaning : partOfSpeech.getMeaningsList()) {
                    builder.append(meaning.getMeaning());
                    builder.append("\n");
                }
            }
            scrambledWordDefinition.setText(builder.toString());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.empty)
                .setMessage(getString(R.string.noWordsGiveScore, numberCorrectAnswers, numberQuestions))
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        scrambledWord.setText(R.string.noWords);
                        scrambledWordDefinition.setText("");
                        submitAnswer.setEnabled(false);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.restart, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        numberCorrectAnswers = 0;
                        Collections.addAll(remainingWords, vocabWords);
                        wordProgress.setProgress(0);
                        if (!submitAnswer.isEnabled()) {
                            submitAnswer.setEnabled(true);
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
     * @param outState Bundle to save teh data
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentText", enteredWord.getText().toString());
        outState.putString("currentWord", scrambledWord.getText().toString());
        outState.putString("currentDefinition", scrambledWordDefinition.getText().toString());
        outState.putInt("progress", wordProgress.getProgress());

    }

    /**
     * Sets the list of vocabwords to be used in the game
     * Creates an arraylist of remainingWords, which (for now, is vocabWords).
     * -Is arrayList so words can easily be removed
     *
     * @param vocabWords
     */
    public void setVocabWords(VocabWord[] vocabWords) {
        this.vocabWords = vocabWords;
        this.numberQuestions = this.vocabWords.length;
        this.remainingWords = new ArrayList<>(numberQuestions);
        Collections.addAll(this.remainingWords, vocabWords);
    }
}
