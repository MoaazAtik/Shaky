package com.thewhitewings.vibro;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
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

    private static final String TAG = "NotesFragment";

    private TextView content1, content2, content3, content4, content5;
    private ImageView icon1, icon2, icon3, icon4, icon5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: ");

        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        RelativeLayout titleView1 = view.findViewById(R.id.title_view1);
        RelativeLayout titleView2 = view.findViewById(R.id.title_view2);
        RelativeLayout titleView3 = view.findViewById(R.id.title_view3);
        RelativeLayout titleView4 = view.findViewById(R.id.title_view4);
        RelativeLayout titleView5 = view.findViewById(R.id.title_view5);
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

        // Show 'Permission Required' note only for Android 13, API >= 33
        if (Build.VERSION.SDK_INT < 33)
            titleView5.setVisibility(View.GONE);

        editContent1();

        return view;
    }


    private void toggleExpand(TextView content,ImageView icon) {
        if (content.getVisibility() == View.VISIBLE) { // collapsed note
            // dismiss the note
            content.setVisibility(View.GONE);
            // Rotate the expand icon
            icon.animate().rotation(0).start();
        } else { // expanded note
            // show the note
            content.setVisibility(View.VISIBLE);
            // Rotate the expand icon
            icon.animate().rotation(180).start();
        }
    }

    private void editContent1() {

        // Initialize the content text
        String contentText1 = getString(R.string.note_content_background_operation_explanation);

        String specs = MainActivity.getDeviceSpecs();
        // Replace text "specifications" with the device's specifications
        contentText1 = contentText1.replace("..specifications..", specs);

        // Create a SpannableStringBuilder which allows to change the markup and content of a text
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(contentText1);

        // Create a ClickableSpan for the link
        ClickableSpan linkClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Handle the link click here
                String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();
                String websiteUrl = "https://dontkillmyapp.com/" + manufacturer;
                Uri uri = Uri.parse(websiteUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        };
        // Apply the ClickableSpan to the link part
        spannableStringBuilder.setSpan(linkClickableSpan,
                contentText1.indexOf("Dontkillmyapp.com"),
                contentText1.indexOf("Dontkillmyapp.com") + "Dontkillmyapp.com".length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply the larger text size to the Device's specifications part
        AbsoluteSizeSpan textSizeSpan = new AbsoluteSizeSpan(18, true);
        spannableStringBuilder.setSpan(textSizeSpan,
                contentText1.indexOf(specs),
                contentText1.indexOf(specs) + specs.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the TextView with the updated SpannableStringBuilder
        content1.setText(spannableStringBuilder);
        content1.setMovementMethod(LinkMovementMethod.getInstance());
    }//editContent1()

}
