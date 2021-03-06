package com.lawrenceqiu.yilaword.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.lawrenceqiu.yilaword.app.vocablist.MainActivity;
import com.lawrenceqiu.yilaword.app.vocablist.VocabWordAdapter;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.VocabWord;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lawrence on 6/20/2015.
 */
public class ReviewWordsFragment extends android.support.v4.app.Fragment
        implements VocabWordAdapter.VocabWordAdapterCallback {
    private RecyclerView mRecyclerView;
    private List<VocabWord> mReviewList;

    /**
     * Inflates the layout view and initializes the recyclerView
     *
     * @param inflater           Inflater
     * @param container          View container
     * @param savedInstanceState Bundle of data
     * @return Inflated view
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_words, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.reviewWordsList);
        return view;
    }

    /**
     * Gets the list of review words and sets up the recylerView's adapter
     *
     * @param savedInstanceState Bundle of data
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mReviewList = getReviewList();
        VocabWordAdapter wordAdapter = new VocabWordAdapter(getActivity(), mReviewList, this);
        mRecyclerView.setAdapter(wordAdapter);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    /**
     * Reads all of the VocabWords stored on the Review List File
     * Reads 10 words if you have signed up and only 5 if you aren't
     * -Loads the Alertdialog if there are no words to read from the file
     *
     * @return List of VocabWords
     */
    private List<VocabWord> getReviewList() {
        List<VocabWord> words = new ArrayList<>();
        File reviewList = new File(getActivity().getFilesDir() + File.separator + Constants.REVIEW_LIST_FILE);
        boolean signedIn = getActivity().getSharedPreferences(Constants.PREFERENCE_FILE, Context.MODE_PRIVATE)
                .getBoolean(Constants.PREFERENCE_LOGIN, false);
        int numWords = signedIn ? 10 : 5;   //Only signed up users can get all 10 of previous day's words
        ObjectInputStream stream = null;    //Unfortunately if you sign up, you don't get all 10 of previous day's words
        try {
            stream = new ObjectInputStream(new FileInputStream(reviewList));
            String readOn = (String) stream.readObject();
            if (readOn.equals(Constants.FILE_FULL)) {
                for (int i = 0; i < numWords; i++) {
                    words.add((VocabWord) stream.readObject());
                }
            } else {
                Toast.makeText(getActivity(), R.string.noReviewWords, Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
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
     * Calls back to the MainActivity and uses the callback to load the meaning based on the position
     *
     * @param position Position selected. Either from 0-4 (Not signed In) or 0-9 (Signed in)
     */
    @Override
    public void itemSelected(int position) {
        MainActivity activity = (MainActivity) getActivity();
        activity.loadMeaning(mReviewList.get(position));
    }
}
