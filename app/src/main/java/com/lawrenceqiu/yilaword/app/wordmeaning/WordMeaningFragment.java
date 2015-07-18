package com.lawrenceqiu.yilaword.app.wordmeaning;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.lawrenceqiu.yilaword.app.Constants;
import com.lawrenceqiu.yilaword.app.R;
import com.lawrenceqiu.yilaword.app.WebDictionaryActivity;
import com.lawrenceqiu.yilaword.app.vocablist.VocabWordDisplayFragment;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.VocabWord;

/**
 * Created by Lawrence on 6/11/2015.
 */
public class WordMeaningFragment extends android.support.v4.app.Fragment {
    private TextView word;
    private TextView wordPartOfSpeech;
    private RecyclerView recyclerView;
    private VocabWord vocabWord;
    private CheckBox knownWord;
    private Button dictionary;
    private KnownWordCallback callback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_meaning, container, false);
        word = (TextView) view.findViewById(R.id.word);
        wordPartOfSpeech = (TextView) view.findViewById(R.id.wordPartOfSpeech);
        knownWord = (CheckBox) view.findViewById(R.id.knownWordCheckBox);
        dictionary = (Button) view.findViewById(R.id.moreInfoDictionary);
        recyclerView = (RecyclerView) view.findViewById(R.id.wordMeaningRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        this.vocabWord = (VocabWord) bundle.getSerializable("VocabWord");
        this.word.setText(vocabWord.getWord());
        this.wordPartOfSpeech.setText(vocabWord.getPartOfSpeech());

        WordMeaningAdapter wordMeaningAdapter = new WordMeaningAdapter(vocabWord);
        recyclerView.setAdapter(wordMeaningAdapter);

        if (vocabWord.isKnownWord()) {
            knownWord.setChecked(true);     //When loaded, check the checkBox is already is a known word
        }

        knownWord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                callback.isKnownWord(isChecked);
            }
        });

        dictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.readInDictionary)
                        .setItems(R.array.dictionaries, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String url = null;
                                switch (which) {
                                    case 0:
                                        url = Constants.VOCABULARY_URL + word.getText().toString();
                                        break;
                                    case 1:
                                        url = Constants.FREE_DICTIONARY_URL + word.getText().toString();
                                        break;
                                    case 2:
                                        url = Constants.MERRIAM_WEBSTER_URL + word.getText().toString();
                                        break;
                                    case 3:
                                        url = Constants.REFERENCE_DICTIONARY_URL + word.getText().toString() + "?s=t";
                                        break;
                                }
                                Intent loadWeb = new Intent(getActivity(), WebDictionaryActivity.class);
                                loadWeb.putExtra("url", url);
                                startActivity(loadWeb);
                            }
                        });
                builder.create().show();
            }
        });
    }

    public void setCallback(KnownWordCallback callback) {
        this.callback = callback;
    }

    public interface KnownWordCallback {
        void isKnownWord(boolean known);
    }
}
