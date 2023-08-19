package com.thewhitewings.vibro;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

public class NotesFragment extends Fragment {

//    private MaterialCardView cardView1;
//    private TextView title1;
//    private TextView content1;
    // Declare for other expandable views

//    private MaterialCardView cardView2;
//    private TextView title2;
//    private TextView content2;

    private RelativeLayout titleView;
//    private FrameLayout contentView;
    private TextView contentView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_notes, container, false);

//        cardView1 = view.findViewById(R.id.card_view1);
//        title1 = view.findViewById(R.id.title1);
//        content1 = view.findViewById(R.id.content1);
        // Initialize other views similarly

//        cardView2 = view.findViewById(R.id.card_view2);
//        title2 = view.findViewById(R.id.title2);
//        content2 = view.findViewById(R.id.content2);

        titleView = view.findViewById(R.id.title_view);
        contentView = view.findViewById(R.id.content_view);


//        cardView1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleExpand(title1, content1);
//            }
//        });
        // Set click listeners for other expandable views

//        cardView2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleExpand(title2, content2);
//            }
//        });

        titleView.setOnClickListener(v -> toggleContentVisibility());


        return view;
    }


    private void toggleContentVisibility() {
        if (contentView.getVisibility() == View.VISIBLE) {
            contentView.setVisibility(View.GONE);
            // Rotate the expand button icon
            ImageView expandButton = titleView.findViewById(R.id.expand_button);
            expandButton.animate().rotation(0).start();
        } else {
            contentView.setVisibility(View.VISIBLE);
            // Rotate the expand button icon
            ImageView expandButton = titleView.findViewById(R.id.expand_button);
            expandButton.animate().rotation(180).start();
        }
    }


    private void toggleExpand(TextView title, TextView content) {
        if (content.getVisibility() == View.VISIBLE) {
            content.setVisibility(View.GONE);
//            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_expand_more_24, 0);
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_launcher_foreground, 0);
        } else {
            content.setVisibility(View.VISIBLE);
//            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_expand_less_24, 0);
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.app_icon_notification, 0);
        }
    }

    //works
//    private void toggleExpand(TextView title, TextView content) {
//        if (content.getVisibility() == View.VISIBLE) {
//            content.setVisibility(View.GONE);
////            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_expand_more_24, 0);
//            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_launcher_foreground, 0);
//        } else {
//            content.setVisibility(View.VISIBLE);
////            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_expand_less_24, 0);
//            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.app_icon_notification, 0);
//        }
//    }
}
