package com.thewhitewings.vibro;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
    TextView txtSpecs1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

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

        txtSpecs1 = view.findViewById(R.id.txt_specs);

        titleView1.setOnClickListener(v -> toggleExpand(content1, icon1));
        titleView2.setOnClickListener(v -> toggleExpand(content2, icon2));
        titleView3.setOnClickListener(v -> toggleExpand(content3, icon3));
        titleView4.setOnClickListener(v -> toggleExpand(content4, icon4));
        titleView5.setOnClickListener(v -> toggleExpand(content5, icon5));

        // Show 'Permission Required' note only for Android 13, API >= 33
        if (Build.VERSION.SDK_INT < 33)
            titleView5.setVisibility(View.GONE);

        Log.d(TAG, "onCreateView: " + 1);
////        TextView contentTextView = view.findViewById(R.id.content1);
//        String content = "Please follow the instructions that align with your device's specifications:\n" +
//                "The guide: dontkillmyapp.com\n\n%s";
//        String clickablePart = "...new specifications...";
//
//        content = String.format(content, clickablePart);
//
//        SpannableString spannableString = new SpannableString(content);
//
//        ClickableSpan linkClickableSpan = new ClickableSpan() {
//            @Override
//            public void onClick(@NonNull View widget) {
//                // Handle the link click here
//                Uri webpage = Uri.parse("https://dontkillmyapp.com");
//                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
//                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
//                    startActivity(intent);
//                }
//            }
//        };
//        spannableString.setSpan(linkClickableSpan, content.indexOf("dontkillmyapp.com"), content.indexOf("dontkillmyapp.com") + "dontkillmyapp.com".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
////        ForegroundColorSpan textColorSpan = new ForegroundColorSpan(Color.BLUE);
////        spannableString.setSpan(textColorSpan, content.indexOf(clickablePart), content.indexOf(clickablePart) + clickablePart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        content1.setText(spannableString);
//        content1.setMovementMethod(LinkMovementMethod.getInstance());

        Log.d(TAG, "onCreateView: " + 2);
//        // Initialize the content text with placeholders for dynamic parts
//        String initialContent = "Please follow the instructions that align with your device's specifications:\n" +
//                "The guide: dontkillmyapp.com\n\n%s";
//        String clickablePart = "..specifications";
//        String dynamicPart = "your device's";
////        Log.d(TAG, "onCreateView: " + initialContent2);
//
////1 first way with one line (instead of 2 below)
////        initialContent = initialContent.replace("your device's", "ann");
//
//        // Combine the parts to create the initial content
////        String formattedContent = String.format(initialContent, clickablePart);
//        String formattedContent = initialContent;
//        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(formattedContent);
//
//        // Create a ClickableSpan for the link
//        ClickableSpan linkClickableSpan = new ClickableSpan() {
//            @Override
//            public void onClick(@NonNull View widget) {
//                // Handle the link click here
//                Uri webpage = Uri.parse("https://dontkillmyapp.com");
//                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
//                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
//                    startActivity(intent);
//                }
//            }
//        };
//
//        // Apply the ClickableSpan to the link part
//        spannableStringBuilder.setSpan(linkClickableSpan,
//                formattedContent.indexOf("dontkillmyapp.com"),
//                formattedContent.indexOf("dontkillmyapp.com") + "dontkillmyapp.com".length(),
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        // Set the initial text
//        TextView content1 = view.findViewById(R.id.content1);
//        content1.setText(spannableStringBuilder);
//        content1.setMovementMethod(LinkMovementMethod.getInstance());
//
//
//
////2
//        // Find the index of the dynamic part in the formatted string
//                int dynamicPartStart = formattedContent.indexOf(dynamicPart);
//                int dynamicPartEnd = dynamicPartStart + dynamicPart.length();
////2
//        // After performing some actions, update the dynamic part
//                dynamicPart = "Android";
//
//
//                //it should be before spannableStringBuilder.replace()
//// Apply the bold style and larger text size to the 'instructions' part
//        StyleSpan boldStyleSpan = new StyleSpan(Typeface.BOLD);
//        AbsoluteSizeSpan textSizeSpan = new AbsoluteSizeSpan(24, true);
//
//// Find the index of the 'instructions' part in the formatted string
//        int instructionsStart = formattedContent.indexOf("The guide");
//        int instructionsEnd = instructionsStart + "The guide".length();
//
//// Apply the spans to the 'instructions' part
//        spannableStringBuilder.setSpan(boldStyleSpan, instructionsStart, instructionsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannableStringBuilder.setSpan(textSizeSpan, instructionsStart, instructionsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//
////2
//        // Update the dynamic part in the SpannableStringBuilder
//                spannableStringBuilder.replace(dynamicPartStart,
//                        dynamicPartEnd, dynamicPart);
//
//
////2
//        // Update the TextView with the updated SpannableStringBuilder
//                content1.setText(spannableStringBuilder);


        Log.d(TAG, "onCreateView: " + 3);
        // Initialize the content text
        String initialContent = getString(R.string.note_b);
        String clickablePart = "..specifications";
        String dynamicPart = "your device's";

//1 first way with one line (instead of 2 below)
//        initialContent = initialContent.replace("your device's", "ann");

        // Combine the parts to create the initial content
//        String formattedContent = String.format(initialContent, clickablePart);
        String formattedContent = initialContent;
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(formattedContent);

        // Create a ClickableSpan for the link
        ClickableSpan linkClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Handle the link click here
                Uri webpage = Uri.parse("https://dontkillmyapp.com");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        };

        // Apply the ClickableSpan to the link part
        spannableStringBuilder.setSpan(linkClickableSpan,
                formattedContent.indexOf("dontkillmyapp.com"),
                formattedContent.indexOf("dontkillmyapp.com") + "dontkillmyapp.com".length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the initial text
        TextView content1 = view.findViewById(R.id.content1);
        content1.setText(spannableStringBuilder);
        content1.setMovementMethod(LinkMovementMethod.getInstance());



//2
        // Find the index of the dynamic part in the formatted string
        int dynamicPartStart = formattedContent.indexOf(dynamicPart);
        int dynamicPartEnd = dynamicPartStart + dynamicPart.length();
//2
        // After performing some actions, update the dynamic part
        dynamicPart = "Android";


        //it should be before spannableStringBuilder.replace()
// Apply the bold style and larger text size to the 'instructions' part
        StyleSpan boldStyleSpan = new StyleSpan(Typeface.BOLD);
        AbsoluteSizeSpan textSizeSpan = new AbsoluteSizeSpan(24, true);

// Find the index of the 'instructions' part in the formatted string
        int instructionsStart = formattedContent.indexOf("The guide");
        int instructionsEnd = instructionsStart + "The guide".length();

// Apply the spans to the 'instructions' part
        spannableStringBuilder.setSpan(boldStyleSpan, instructionsStart, instructionsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(textSizeSpan, instructionsStart, instructionsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


//2
        // Update the dynamic part in the SpannableStringBuilder
        spannableStringBuilder.replace(dynamicPartStart,
                dynamicPartEnd, dynamicPart);


//2
        // Update the TextView with the updated SpannableStringBuilder
        content1.setText(spannableStringBuilder);


        return view;
    }


    private void toggleExpand(TextView content,ImageView icon) {
        if (content.getVisibility() == View.VISIBLE) { // collapsed note
            // dismiss the note
            content.setVisibility(View.GONE);
            // Rotate the expand icon
            icon.animate().rotation(0).start();
//            // Dismiss txtSpecs
//            if (content == content1) {
//                txtSpecs1.setVisibility(View.GONE);
//            }
        } else { // expanded note
            // show the note
            content.setVisibility(View.VISIBLE);
            // Rotate the expand icon
            icon.animate().rotation(180).start();
//            // Show txtSpecs
//            if (content == content1) {
//                txtSpecs1.setText(MainActivity.getDeviceSpecs());
//                txtSpecs1.setVisibility(View.VISIBLE);
//            }
        }
    }

}
