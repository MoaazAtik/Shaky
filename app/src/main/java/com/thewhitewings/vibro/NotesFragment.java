package com.thewhitewings.vibro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NotesFragment extends Fragment {

    private RelativeLayout titleView1, titleView2, titleView3, titleView4, titleView5;
    private TextView content1, content2, content3, content4, content5;
    private ImageView icon1, icon2, icon3, icon4, icon5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        titleView1 = view.findViewById(R.id.title_view1);
        titleView2 = view.findViewById(R.id.title_view2);
        titleView3 = view.findViewById(R.id.title_view3);
        titleView4 = view.findViewById(R.id.title_view4);
        titleView5 = view.findViewById(R.id.title_view5);
        content1 = view.findViewById(R.id.content1);
        content2 = view.findViewById(R.id.content2);
        content3 = view.findViewById(R.id.content3);
        content4 = view.findViewById(R.id.content4);
        content5 = view.findViewById(R.id.content5);
        icon1 = view.findViewById(R.id.icon1);
        icon2 = view.findViewById(R.id.icon2);
        icon3 = view.findViewById(R.id.icon3);
        icon4 = view.findViewById(R.id.icon4);
        icon5 = view.findViewById(R.id.icon5);

        titleView1.setOnClickListener(v -> toggleExpand(content1, icon1));
        titleView2.setOnClickListener(v -> toggleExpand(content2, icon2));
        titleView3.setOnClickListener(v -> toggleExpand(content3, icon3));
        titleView4.setOnClickListener(v -> toggleExpand(content4, icon4));
        titleView5.setOnClickListener(v -> toggleExpand(content5, icon5));

        return view;
    }


    private void toggleExpand(TextView content,ImageView icon) {
        if (content.getVisibility() == View.VISIBLE) {
            content.setVisibility(View.GONE);
            // Rotate the expand button icon
            icon.animate().rotation(0).start();
        } else {
            content.setVisibility(View.VISIBLE);
            // Rotate the expand button icon
            icon.animate().rotation(180).start();
        }
    }

}
