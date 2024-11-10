package com.thewhitewings.shaky.ui.notes;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.thewhitewings.shaky.R;
import com.thewhitewings.shaky.Util;
import com.thewhitewings.shaky.databinding.FragmentNotesBinding;

public class NotesFragment extends Fragment {

    private static final String TAG = "NotesFragment";

    private FragmentNotesBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotesBinding.inflate(inflater, container, false);

        // Hide 'Permission Required' note for Android API < 33 because it's not needed
        if (Build.VERSION.SDK_INT < 33)
            binding.card5.setVisibility(View.GONE);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUiComponents();

        // Make some edits to the content of the first note
        editContent1();
        // Add a hyperlink to the content of the sixth note
        editContent6();
    }

    private void setupUiComponents() {
        // Call toggleExpand() when the notes' titles are clicked
        binding.titleView1.setOnClickListener(v -> toggleExpand(binding.content1, binding.icon1));
        binding.titleView2.setOnClickListener(v -> toggleExpand(binding.content2, binding.icon2));
        binding.titleView3.setOnClickListener(v -> toggleExpand(binding.content3, binding.icon3));
        binding.titleView4.setOnClickListener(v -> toggleExpand(binding.content4, binding.icon4));
        binding.titleView5.setOnClickListener(v -> toggleExpand(binding.content5, binding.icon5));
        binding.titleView6.setOnClickListener(v -> toggleExpand(binding.content6, binding.icon6));
    }


    private void toggleExpand(TextView content, ImageView icon) {
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

    // Edit the content of the first note
    // Show the device specifications and increase their font size, and hyperlink the web links by adding clickable spans to them
    private void editContent1() {
        // Initialize the content text
        String contentText1 = getString(R.string.note_content_background_operation_explanation);

        String specs = Util.getDeviceSpecs();
        // Replace text "specifications" with the device's specifications
        contentText1 = contentText1.replace("..specifications..", specs);

        // Create a SpannableStringBuilder which allows to change the markup and content of a text
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(contentText1);


        // - Create a ClickableSpan for the first link
        ClickableSpan linkClickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Uri uri = Util.getBatteryOptimizationGuideUri1();
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Opening guide website 1 failed: ", e);
                }
            }
        };
        String guideTextString1 = "Dontkillmyapp.com";
        // Apply the ClickableSpan to the first link part
        spannableStringBuilder.setSpan(linkClickableSpan1,
                contentText1.indexOf(guideTextString1),
                contentText1.indexOf(guideTextString1) + guideTextString1.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        // - Create a ClickableSpan for the second link
        ClickableSpan linkClickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Uri uri = Util.getBatteryOptimizationGuideUri2();
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Opening guide website 2 failed: ", e);
                }
            }
        };
        String guideTextString2 = "Bark.us";
        // Apply the ClickableSpan to the second link part
        spannableStringBuilder.setSpan(linkClickableSpan2,
                contentText1.indexOf(guideTextString2),
                contentText1.indexOf(guideTextString2) + guideTextString2.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        // - Apply the larger text size to the Device's specifications part
        AbsoluteSizeSpan textSizeSpan = new AbsoluteSizeSpan(18, true);
        spannableStringBuilder.setSpan(textSizeSpan,
                contentText1.indexOf(specs),
                contentText1.indexOf(specs) + specs.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        // Update the TextView with the updated SpannableStringBuilder
        binding.content1.setText(spannableStringBuilder);
        binding.content1.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // Edit the content of the sixth note
    // Show the device specifications and increase their font size, and hyperlink the web links by adding clickable spans to them
    private void editContent6() {
        // Initialize the content text
        String contentText6 = getString(R.string.note_content_privacy_policy);

        // Create a SpannableString which allows to change the markup of a text
        SpannableString spannableString = new SpannableString(contentText6);

        // Create a ClickableSpan for the link
        ClickableSpan linkClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Handle the link click
                String websiteUrl = "https://sites.google.com/view/shaky-privacy-policy";
                Uri uri = Uri.parse(websiteUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Opening privacy policy website failed: ", e);
                }
            }
        };
        String privacyPolicyTextString = "shaky-privacy-policy";
        // Apply the ClickableSpan to the link part
        spannableString.setSpan(linkClickableSpan,
                contentText6.indexOf(privacyPolicyTextString),
                contentText6.indexOf(privacyPolicyTextString) + privacyPolicyTextString.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the TextView with the updated SpannableString
        binding.content6.setText(spannableString);
        binding.content6.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}