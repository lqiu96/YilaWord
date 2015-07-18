package com.lawrenceqiu.yilaword.app.wordmeaning;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.lawrenceqiu.yilaword.app.R;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.Meaning;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.partofspeech.PartOfSpeech;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.VocabWord;

import java.util.ArrayList;

/**
 * Created by Lawrence on 6/11/2015.
 */
public class WordMeaningAdapter extends RecyclerView.Adapter<WordMeaningAdapter.WordMeaningHolder> {
    private VocabWord vocabWord;
    private ArrayList<PartOfSpeech> partOfSpeeches;

    public WordMeaningAdapter(VocabWord vocabWord) {
        this.vocabWord = vocabWord;
        partOfSpeeches = vocabWord.getPartOfSpeeches(); //List of Parts of Speeches: Nouns, Verbs, etc..
    }                                                   //Could be 1, could be 4

    public static class WordMeaningHolder extends RecyclerView.ViewHolder {
        private TextView wordMeaning;
//        private TextView exampleOne;  Removed because only one example per meaning
//        private TextView exampleTwo;
//        private TextView exampleThree;

        public WordMeaningHolder(View itemView) {
            super(itemView);
            wordMeaning = (TextView) itemView.findViewById(R.id.wordMeaning);
//            exampleOne = (TextView) itemView.findViewById(R.id.exampleOne);
//            exampleTwo = (TextView) itemView.findViewById(R.id.exampleTwo);
//            exampleThree = (TextView) itemView.findViewById(R.id.exampleThree);
        }
    }

    @Override
    public WordMeaningHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.word_meaning_item, viewGroup, false);
        return new WordMeaningHolder(view);
    }

    @Override
    public void onBindViewHolder(WordMeaningHolder holder, int position) {
        //Based on the position of the arraylist-- Noun is usually first, followed by the rest
        ArrayList<Meaning> meanings = partOfSpeeches.get(position).getMeaningsList();       //ArrayList of Meaning objects
        /*holder.wordMeaning.setText(meanings.get(position).getMeaning());
        holder.exampleOne.setText(meanings.get(position).getExample());     //This should be correct*/
        //Above needs to be modified so that each tile represents a Part Of speech. Position is incorrectly refered to otherwise
        //So, each tile has infinite number of means that are able to be stored as long as there are that many meanings for the
        //part of speech
        StringBuilder builder = new StringBuilder();
        for (Meaning meaning : meanings) {
            builder.append(meaning.getMeaning())
                    .append("\n<p>")          //Example is ona new line and in paragraph tags so they wrap
                    .append(meaning.getExample())   //Actually, I don't think the <p> tags do anything..
                    .append("</p>");
        }
        holder.wordMeaning.setText(Html.fromHtml(builder.toString()));  //Html.fromHtml allows me to use <p> tags
    }

    /**
     * Get the number of PartOfSpeeches in the word
     * Since each partofspeech gets its own tile
     *
     * @return Number of Part Of Speeches int he word
     */
    @Override
    public int getItemCount() {
        return partOfSpeeches.size();
    }

    @Override
    public void onViewAttachedToWindow(WordMeaningHolder holder) {
        super.onViewAttachedToWindow(holder);
    }
}
