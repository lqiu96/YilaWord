package com.lawrenceqiu.yilaword.app.vocablist;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.lawrenceqiu.yilaword.app.R;
import com.lawrenceqiu.yilaword.app.ReviewWordsFragment;
import com.lawrenceqiu.yilaword.app.vocabwordstructure.VocabWord;

import java.util.List;

/**
 * Created by Lawrence on 6/10/2015.
 */
public class VocabWordAdapter extends RecyclerView.Adapter<VocabWordAdapter.WordHolder> {
    private Activity mActivity;
    private List<VocabWord> mWordsList;
    private VocabWordAdapterCallback mCallback;

    /**
     * Call back to get the position selected from the RecyclerView
     */
    public interface VocabWordAdapterCallback {
        void itemSelected(int position);
    }

    /**
     * Adapter's constructor
     *
     * @param mActivity Fragment's activity
     * @param reviewList List of Vocabwords to display
     * @param fragment Fragment that called it
     */
    public VocabWordAdapter(Activity mActivity, List<VocabWord> reviewList, ReviewWordsFragment fragment) {
        this.mActivity = mActivity;
        this.mWordsList = reviewList;
        this.mCallback = fragment;
    }

    /**
     * Adapter's constructor
     *  @param mActivity Fragment's activity
     * @param mWordsList List of Vocabwords to display
     * @param fragment Fragment that called it
     */
    public VocabWordAdapter(Activity mActivity, List<VocabWord> mWordsList, VocabWordDisplayFragment fragment) {
        this.mActivity = mActivity;
        this.mWordsList = mWordsList;
        this.mCallback = fragment;
    }

    public static class WordHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView wordName;
        private TextView partOfSpeech;
        private TextView numberDefinitions;
        private VocabWordAdapterCallback callback;

        public WordHolder(View itemView, VocabWordAdapterCallback callback) {
            super(itemView);
            this.callback = callback;
            wordName = (TextView) itemView.findViewById(R.id.wordName);
            partOfSpeech = (TextView) itemView.findViewById(R.id.partOfSpeech);
            numberDefinitions = (TextView) itemView.findViewById(R.id.numberDefinitions);
            itemView.setOnClickListener(this);  //Listener to get which position was selected
        }

        /**
         * When user clicks on VocabWord's tile, it gives back the position (which index) the tile is
         * @param v View
         */
        @Override
        public void onClick(View v) {
            callback.itemSelected(getLayoutPosition());
        }
    }

    /**
     * Inflates the view and returns a WoldHolder class
     *
     * @param viewGroup viewGroup
     * @param i Position of the view
     * @return WorldHolder class
     */
    public WordHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.word_display_item, viewGroup, false);
        return new WordHolder(view, mCallback);
    }

    @Override
    public void onBindViewHolder(WordHolder holder, int position) {
        VocabWord word = mWordsList.get(position);        //Based on the position on the RecycleView the user pressed
        holder.wordName.setText(word.getWord());
        String speech = word.getPartOfSpeech();
        if (speech.length() >= 15) {
            speech = speech.substring(0, 13) + "..";
        }
        holder.partOfSpeech.setText(speech);
//        holder.partOfSpeech.setText(word.getPartOfSpeech());
        int numberDefinitions = word.getNumberMeanings();
        //Based on a pluraity String. Based on the input value, it gives back a certain string
        holder.numberDefinitions.setText(mActivity.getResources().getQuantityString(R.plurals.numberMeaningsSet,
                numberDefinitions, numberDefinitions));
    }

    /**
     * Number of elements in the arraylist
     * -If the app doesn't get the words from the server or doesn't finish parsing (for whatever reason), the size is 0
     *
     * @return Size of the WordList
     */
    @Override
    public int getItemCount() {
        return mWordsList.size();
    }

    @Override
    public void onViewAttachedToWindow(WordHolder holder) {
        super.onViewAttachedToWindow(holder);
    }
}
