package com.lawrenceqiu.yilaword.app.vocabwordstructure;

import java.io.Serializable;

/**
 * Created by Lawrence on 6/13/2015.
 */
public class Meaning implements Serializable {
    private String meaning;
    private String example;

    public Meaning(String meaning, String example) {
        this.meaning = meaning;
        this.example = example;
    }

    public String getMeaning() {
        return meaning;
    }

    public String getExample() {
        return example;
    }
}
