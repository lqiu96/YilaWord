package com.lawrenceqiu.yilaword.app.vocablist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.lawrenceqiu.yilaword.app.Constants;
import com.lawrenceqiu.yilaword.app.R;
import com.lawrenceqiu.yilaword.app.registerlogin.RegisterLoginActivity;
import com.lawrenceqiu.yilaword.app.reviewwords.ReviewWordsFragment;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.VocabWord;
import com.lawrenceqiu.yilaword.app.wordmeaning.WordMeaningFragment;

public class MainActivity extends AppCompatActivity {
    private MenuItem signInSignOut;
    private SharedPreferences sharedPreferences;
    private VocabWordDisplayFragment vocabWordDisplayFragment;
    /**
     * Listener for any changes in sharedPreferences
     * -Changes dealing with login will change the menuItem's text to the other option
     * -"Sign In" becomes "Sign Out"
     * -"Sign Out" becomes "Sign In"
     * Then, Refreshes the menu
     */
    private SharedPreferences.OnSharedPreferenceChangeListener handleSignInSignOutListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    boolean signedIn = sharedPreferences.getBoolean(Constants.PREFERENCE_LOGIN, false);
                    if (signedIn) {
                        signInSignOut.setTitle(R.string.signOut);
                    } else {
                        signInSignOut.setTitle(R.string.signIn);
                    }
                    invalidateOptionsMenu(); //Refresh the menu to display the new message
                }
            };

    /**
     * Creates the MainActivity by loading up the layout and setting up the toolbar
     * Checks the app needs to get the daily words from the server and does it needs to update the list
     *
     * @param savedInstanceState Bundle of saved data
     */
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("onCreate", "in MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mainActivityToolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        vocabWordDisplayFragment = new VocabWordDisplayFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.wordDisplayContainer,
                vocabWordDisplayFragment, "vocabWords").commit();
    }

    /**
     * Add the fragment to the container when it is started (created) or when the user screen is rotated
     * <p/>
     * Get the shared preferences and add the listener
     */
    @Override
    protected void onStart() {
        super.onStart();

        sharedPreferences = getSharedPreferences(Constants.PREFERENCE_FILE, Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(handleSignInSignOutListener);
    }

    /**
     * This is only created once, when the activity is created
     *
     * @param menu Menu
     * @return true that the Menu has been created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        signInSignOut = menu.findItem(R.id.action_signInSignOut);
        boolean signedIn = sharedPreferences.getBoolean(Constants.PREFERENCE_LOGIN, false);
        if (signedIn) {
            signInSignOut.setTitle(R.string.signOut);
        } else {
            signInSignOut.setTitle(R.string.signIn);
        }
        return true;
    }

    /**
     * If user wants to sign out, confirm that they want to log out with the dialog fragment
     * If user wants to sign in, open intent to load the RegisterLoginActivity
     *
     * @param item Selected MenuItem
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_signInSignOut:
                boolean signedIn = sharedPreferences.getBoolean(Constants.PREFERENCE_LOGIN, false);
                if (!signedIn) {    //Load intent to bring up activity for user to sign in
                    Intent intent = new Intent(MainActivity.this, RegisterLoginActivity.class);
                    startActivity(intent);
                } else {
                    /*
                        No need to explicitly create new class that extends DialogFragment
                        Even when screen rotates, it still reappears.
                    */
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.signOut)
                            .setMessage(R.string.confirmSignOut)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    signInSignOut.setTitle(R.string.signOut);
                                    SharedPreferences sharedPreferences = getSharedPreferences(
                                            Constants.PREFERENCE_FILE, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(Constants.PREFERENCE_LOGIN, false);
                                    editor.apply();
                                }
                            });
                    builder.create().show();
                }
                invalidateOptionsMenu();        //Refresh menu
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Add the meaning Fragment and add the VocabWord to the fragment
     * Replace the container with the new Word meaning fragment
     *
     * @param vocabWord Vocab Word to pass
     */
    public void loadMeaning(VocabWord vocabWord) {
        WordMeaningFragment wordMeaningFragment = new WordMeaningFragment();
        wordMeaningFragment.setCallback(vocabWordDisplayFragment);
        Bundle bundle = new Bundle();
        bundle.putSerializable("VocabWord", vocabWord);
        wordMeaningFragment.setArguments(bundle);
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_LARGE) {     //Large screen only
            getSupportFragmentManager().beginTransaction().add(R.id.wordMeaningContainer, wordMeaningFragment,
                    "meaning").addToBackStack(null).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.wordDisplayContainer, wordMeaningFragment,
                    "meaning").addToBackStack(null).commit();
        }
    }

    /**
     * Replace the container with the new fragment (Review words)
     */
    public void loadReviewWords() {
        ReviewWordsFragment reviewWordsFragment = new ReviewWordsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.wordDisplayContainer, reviewWordsFragment,
                "review").addToBackStack(null).commit();
    }
}
