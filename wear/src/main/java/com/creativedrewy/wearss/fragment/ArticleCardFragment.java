package com.creativedrewy.wearss.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.CardFrame;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creativedrewy.wearss.R;

/**
 * Card fragment that shows an individual headline and short/long description
 */
public class ArticleCardFragment extends Fragment {
    private static final String TITLE_EXTRA = "param1";
    private static final String DESC_EXTRA = "param2";

    private String mTitleText = "";
    private String mDescText = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTitleText = getArguments().getString(TITLE_EXTRA);
            mDescText = getArguments().getString(DESC_EXTRA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_card, container, false);

        CardFrame frame = (CardFrame) view.findViewById(R.id.main_frame);
        frame.setExpansionFactor(200);

        TextView titleTextView = (TextView) view.findViewById(R.id.article_title_textview);
        TextView descTextView = (TextView) view.findViewById(R.id.article_desc_textview);

        titleTextView.setText(mTitleText);
        descTextView.setText(mDescText);

        return view;
    }

    /**
     * Create a new instance of the article view, passing in the top headline and the contents
     * that go underneath
     */
    public static ArticleCardFragment newInstance(String title, String remainingContent) {
        ArticleCardFragment fragment = new ArticleCardFragment();
        Bundle args = new Bundle();

        args.putString(TITLE_EXTRA, title);
        args.putString(DESC_EXTRA, remainingContent);
        fragment.setArguments(args);

        return fragment;
    }
}