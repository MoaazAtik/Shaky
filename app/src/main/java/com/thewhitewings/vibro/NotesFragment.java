package com.thewhitewings.vibro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

public class NotesFragment extends Fragment {

    private MaterialCardView cardView1;
    private TextView title1;
    private TextView content1;
    // Declare for other expandable views


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        cardView1 = view.findViewById(R.id.card_view1);
        title1 = view.findViewById(R.id.title1);
        content1 = view.findViewById(R.id.content1);
        // Initialize other views similarly

        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleExpand(title1, content1);
            }
        });
        // Set click listeners for other expandable views

        return view;
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
}
