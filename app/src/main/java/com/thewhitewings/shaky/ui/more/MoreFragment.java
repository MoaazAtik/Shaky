package com.thewhitewings.shaky.ui.more;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.thewhitewings.shaky.R;
import com.thewhitewings.shaky.ShakyApplication;
import com.thewhitewings.shaky.data.ShakyPreferences;
import com.thewhitewings.shaky.databinding.FragmentMoreBinding;
import com.thewhitewings.shaky.ui.notes.NotesFragment;

public class MoreFragment extends Fragment {

    private static final String TAG = "MoreFragment";

    private FragmentMoreBinding binding;

    // defaultToneResource
    private String rawResourceString;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUiComponents();
    }

    private void setupUiComponents() {
        binding.btnTone.setOnClickListener(v -> pickTone());
        binding.btnFeedback.setOnClickListener(v -> sendEmail());
        binding.btnNotes.setOnClickListener(v -> navigateToNotesFragment());
    }

    private void pickTone() {
        int rawResourceId = R.raw.soft;
        rawResourceString = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                getResources().getResourcePackageName(rawResourceId) + '/' +
                getResources().getResourceTypeName(rawResourceId) + '/' +
                getResources().getResourceEntryName(rawResourceId);
        // Get the Uri of the default tone
        Uri rawResourceUri = Uri.parse(rawResourceString);

        // Create an intent to open the ringtone picker to change the alarm tone of the app
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);//to choose from internal (ringtones) storage
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, rawResourceUri);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);

        // Open the ringtone picker
        tonePickerActivityResultLauncher.launch(intent);
    }

    private void navigateToNotesFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Animations. this has to be before fragmentTransaction.replace()
        fragmentTransaction.setCustomAnimations(
                androidx.fragment.R.animator.fragment_fade_enter, // Enter animation
                androidx.fragment.R.animator.fragment_fade_exit, // Exit animation
                androidx.fragment.R.animator.fragment_close_enter, // Pop enter animation (when navigating back)
                androidx.fragment.R.animator.fragment_fade_exit // Pop exit animation (when navigating back)
        );

        fragmentTransaction.replace(R.id.fragment_container_notes, new NotesFragment());
        fragmentTransaction.addToBackStack(null); // Optional, for back navigation

        fragmentTransaction.commit();
    }

    private final ActivityResultCallback<ActivityResult> tonePickerCallback = result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            // Get the Uri of the selected tone
            Uri selectedToneUri =
                    result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            ShakyPreferences preferences = ((ShakyApplication) requireActivity().getApplicationContext())
                    .getPreferences();
            // If a tone is picked, save it to the preferences
            if (selectedToneUri != null) {
                preferences.updateAlarmTonePreference(selectedToneUri.toString());
            } else {
                // If no tone is picked, save the previously selected tone with the ALARM_TONE_KEY if one exists
                String previousToneStr = preferences.getAlarmTonePreference(rawResourceString);
                preferences.updateAlarmTonePreference(previousToneStr);
            }
        }
    };

    private final ActivityResultLauncher<Intent> tonePickerActivityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    tonePickerCallback
            );

    private void sendEmail() {
        // Create an intent with recipientEmail, emailSubject, and emailBody to send feedback via an email app
        String recipientEmail = getString(R.string.recipient_email_address);
        String emailSubject = getString(R.string.email_subject);
        String emailBody = getString(R.string.email_body);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        // If there are email apps on the device, open a chooser to select one
        if (emailIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_feedback_email_app_chooser)));
        } else {
            Toast.makeText(requireContext(), R.string.no_email_app_found, Toast.LENGTH_SHORT).show();
            Toast.makeText(requireActivity(), getString(R.string.your_feedback_is_welcome_at) + "\n" +
                            getString(R.string.recipient_email_address), Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}