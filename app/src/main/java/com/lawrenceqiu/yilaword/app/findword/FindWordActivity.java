package com.lawrenceqiu.yilaword.app.findword;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.lawrenceqiu.yilaword.app.R;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.VocabWord;

import java.util.ArrayList;

/**
 * Created by Lawrence on 6/29/2015.
 */
public class FindWordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_word);

        Toolbar toolbar = (Toolbar) findViewById(R.id.meaningWordToolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {   //No way it is going to be null, only call this is called is by intent
            int numWords = bundle.getInt("numWords");
            ArrayList<VocabWord> vocabWords = new ArrayList<>();
            for (int i = 0; i < numWords; i++) {
                vocabWords.add((VocabWord) bundle.getSerializable("word" + i));
            }
            FindWordFragment fragment = (FindWordFragment) getFragmentManager()
                    .findFragmentById(R.id.meaningWordFragment);
            fragment.setVocabWords(vocabWords);
        }
    }
}
