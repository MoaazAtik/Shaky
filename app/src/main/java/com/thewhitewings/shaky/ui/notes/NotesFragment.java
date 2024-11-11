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

/**
 * Fragment that displays important notes about the app
 */
public class NotesFragment extends Fragment {

    private static final String TAG = "NotesFragment";

    // Binding object instance corresponding to the fragment_notes.xml layout
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

    /**
     * Set up the UI components
     */
    private void setupUiComponents() {
        // Call toggleExpand() when the notes' titles are clicked
        binding.titleView1.setOnClickListener(v -> toggleExpand(binding.content1, binding.icon1));
        binding.titleView2.setOnClickListener(v -> toggleExpand(binding.content2, binding.icon2));
        binding.titleView3.setOnClickListener(v -> toggleExpand(binding.content3, binding.icon3));
        binding.titleView4.setOnClickListener(v -> toggleExpand(binding.content4, binding.icon4));
        binding.titleView5.setOnClickListener(v -> toggleExpand(binding.content5, binding.icon5));
        binding.titleView6.setOnClickListener(v -> toggleExpand(binding.content6, binding.icon6));
    }


    /**
     * Toggle the visibility of the content of the note
     *
     * @param content The content of the note to be toggled
     * @param icon    The expand icon of the note to be animated
     */
    private void toggleExpand(TextView content, ImageView icon) {
        // If the note is expanded
        if (content.getVisibility() == View.VISIBLE) {
            // Dismiss the note
            content.setVisibility(View.GONE);
            // Rotate the expand icon
            icon.animate().rotation(0).start();

            // If the note is dismissed
        } else {
            // Show the note
            content.setVisibility(View.VISIBLE);
            // Rotate the expand icon
            icon.animate().rotation(180).start();
        }
    }

    /**
     * Edit the content of the first note.
     * <br>
     * Show the device specifications and increase their font size,
     * and hyperlink the web links by adding clickable spans to them
     */
    private void editContent1() {
        // Get the content text
        String contentText1 = getString(R.string.note_content_background_operation_explanation);

        // Get the device's specifications
        String specs = Util.getDeviceSpecs();
        // Replace text "specifications" with the device's specifications
        contentText1 = contentText1.replace("..specifications..", specs);

        // Create a SpannableStringBuilder which allows to change the markup and content of a text
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(contentText1);


        // - Create a ClickableSpan for the first link to open the guide website when clicked
        ClickableSpan linkClickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Get the URI of the first guide
                Uri uri = Util.getBatteryOptimizationGuideUri1();
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    // Open the link in a browser
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Opening guide website 1 failed: ", e);
                }
            }
        };
        // Apply the ClickableSpan to the first link part
        String guideTextString1 = "Dontkillmyapp.com";
        spannableStringBuilder.setSpan(
                linkClickableSpan1,
                contentText1.indexOf(guideTextString1),
                contentText1.indexOf(guideTextString1) + guideTextString1.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );


        // - Create a ClickableSpan for the second link to open the guide website when clicked
        ClickableSpan linkClickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Get the URI of the second guide
                Uri uri = Util.getBatteryOptimizationGuideUri2();
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    // Open the link in a browser
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Opening guide website 2 failed: ", e);
                }
            }
        };
        // Apply the ClickableSpan to the second link part
        String guideTextString2 = "Bark.us";
        spannableStringBuilder.setSpan(
                linkClickableSpan2,
                contentText1.indexOf(guideTextString2),
                contentText1.indexOf(guideTextString2) + guideTextString2.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );


        // - Apply larger text size to the Device's specifications part
        AbsoluteSizeSpan textSizeSpan = new AbsoluteSizeSpan(18, true);
        spannableStringBuilder.setSpan(
                textSizeSpan,
                contentText1.indexOf(specs),
                contentText1.indexOf(specs) + specs.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );


        // Update the TextView with the updated SpannableStringBuilder
        binding.content1.setText(spannableStringBuilder);
        // Enable the movement method to detect the clickable links
        // for handling arrow key movement for this TextView
        binding.content1.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Edit the content of the sixth note.
     * <br>
     * Hyperlink the web link by adding clickable spans to it.
     */
    private void editContent6() {
        // Get the content text
        String contentText6 = getString(R.string.note_content_privacy_policy);

        // Create a SpannableString which allows to change the markup of a text
        SpannableString spannableString = new SpannableString(contentText6);

        // - Create a ClickableSpan for the link to open the privacy policy website when clicked
        ClickableSpan linkClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String websiteUrl = "https://sites.google.com/view/shaky-privacy-policy";
                Uri uri = Uri.parse(websiteUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    // Open the link in a browser
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Opening privacy policy website failed: ", e);
                }
            }
        };
        // Apply the ClickableSpan to the link part
        String privacyPolicyTextString = "shaky-privacy-policy";
        spannableString.setSpan(
                linkClickableSpan,
                contentText6.indexOf(privacyPolicyTextString),
                contentText6.indexOf(privacyPolicyTextString) + privacyPolicyTextString.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Update the TextView with the updated SpannableString
        binding.content6.setText(spannableString);
        // Enable the movement method to detect the clickable links
        // for handling arrow key movement for this TextView
        binding.content6.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}