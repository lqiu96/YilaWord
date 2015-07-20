package com.lawrenceqiu.yilaword.app.scrambledword;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.lawrenceqiu.yilaword.app.Constants;
import com.lawrenceqiu.yilaword.app.R;
import com.lawrenceqiu.yilaword.app.WebDictionaryActivity;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.VocabWord;

/**
 * Created by Lawrence on 6/11/2015.
 */
public class ScrambledWordActivity extends AppCompatActivity {
    private ScrambledWordFragment mScrambledWordFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrambled_word);

        Toolbar toolbar = (Toolbar) findViewById(R.id.spellingFunToolBar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        mScrambledWordFragment = (ScrambledWordFragment) getFragmentManager()
                .findFragmentById(R.id.spellingFunFragment);

        Bundle bundle = getIntent().getExtras();
        int size = bundle.getInt("NumWords");
        VocabWord[] words = new VocabWord[size];
        for (int i = 0; i < size; i++) {
            words[i] = ((VocabWord) bundle.getSerializable("word" + i));
        }
        mScrambledWordFragment.setVocabWords(words);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spelling_fun, menu);
        return true;
    }

    /**
     * Handles when the user either selects the 'Give up' or 'More' button
     * If user selects 'Give up' button, an AlertDialog pops up informing the user of the correctly
     * unscrambled word. While displaying the word, it sets up the next word so the user cannot
     * entered the displayed answer
     * If the user selects 'More' button, creates an intent to load up the word to be searched
     * through dictionary.com. Also loads the next word
     *
     * @param item MenuItem that was selected
     * @return true or false (calls the super class)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_giveUp:
                if (!mScrambledWordFragment.remainingWords.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.reveal)
                            .setMessage("The answer is " + mScrambledWordFragment.scrambledAnswer)
                            .setCancelable(false)
                            .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                }
                break;
            case R.id.action_info:
                final String word = mScrambledWordFragment.scrambledAnswer;
                AlertDialog.Builder builder = new  AlertDialog.Builder(this)
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
                                Intent loadWeb = new Intent(ScrambledWordActivity.this, WebDictionaryActivity.class);
                                loadWeb.putExtra("url", url);
                                startActivity(loadWeb);
                            }
                        });
                builder.create().show();
                break;
        }
        //Both of these move to the next word
        mScrambledWordFragment.setUpWord();
        //Progress throughout the game increases, even though user didn't correctly answer
        mScrambledWordFragment.wordProgress.setProgress(mScrambledWordFragment.wordProgress.getProgress() + 1);
        return super.onOptionsItemSelected(item);
    }
}
