package com.lawrenceqiu.yilaword.app.vocabwordstructure;

import java.io.Serializable;

/**
 * Created by Lawrence on 6/13/2015.
 */
public class Meaning implements Serializable {
    private String mMeaning;
    private String mExample;

    public Meaning(String mMeaning, String mExample) {
        this.mMeaning = mMeaning;
        this.mExample = mExample;
    }

    public String getMeaning() {
        return mMeaning;
    }

    public String getExample() {
        return mExample;
    }
}
