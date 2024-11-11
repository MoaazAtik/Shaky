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

/**
 * Fragment that displays more actions and information about the app
 */
public class MoreFragment extends Fragment {

    private static final String TAG = "MoreFragment";

    // Binding object instance corresponding to the fragment_more.xml layout
    private FragmentMoreBinding binding;

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

    /**
     * Set up the UI components
     */
    private void setupUiComponents() {
        binding.btnTone.setOnClickListener(v -> pickTone());
        binding.btnFeedback.setOnClickListener(v -> sendEmail());
        binding.btnNotes.setOnClickListener(v -> navigateToNotesFragment());
    }


    /**
     * Open the ringtone picker to change the alarm tone
     */
    private void pickTone() {
        // The raw resource of the default alarm tone
        int rawResourceId = R.raw.soft;
        String rawResourceString = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                getResources().getResourcePackageName(rawResourceId) + '/' +
                getResources().getResourceTypeName(rawResourceId) + '/' +
                getResources().getResourceEntryName(rawResourceId);
        Uri rawResourceUri = Uri.parse(rawResourceString);

        // Create an intent to open the ringtone picker to choose from internal (ringtones) storage
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.tone_picker_title));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, rawResourceUri);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);

        // Open the ringtone picker activity and handle tone selection result
        tonePickerActivityResultLauncher.launch(intent);
    }

    /**
     * Callback for handling the result of the ringtone picker activity.
     * <br>
     * It updates the alarm tone preference if a tone is selected.
     * <br>
     * It is an implementation of {@link ActivityResultCallback}
     * that is used for the implementation of {@link ActivityResultLauncher}.
     */
    private final ActivityResultCallback<ActivityResult> tonePickerCallback = result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            // Get the Uri of the selected tone
            Uri selectedToneUri =
                    result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            // If a tone is picked, save it to the preferences
            if (selectedToneUri != null) {
                ShakyPreferences preferences =
                        ((ShakyApplication) requireActivity().getApplicationContext())
                                .getPreferences();
                preferences.updateAlarmTonePreference(selectedToneUri.toString());
            }
        }
    };

    /**
     * Launcher for the ringtone picker activity with a callback to handle the result
     */
    private final ActivityResultLauncher<Intent> tonePickerActivityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    tonePickerCallback
            );

    /**
     * Send feedback email
     */
    private void sendEmail() {
        // Create an intent with pre-filled values for recipient email, subject, and body
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

            // If there are no email apps on the device, show a toast message with followup instructions
        } else {
            Toast.makeText(requireContext(), R.string.no_email_app_found, Toast.LENGTH_SHORT).show();
            Toast.makeText(requireActivity(), getString(R.string.your_feedback_is_welcome_at) + "\n" +
                            getString(R.string.recipient_email_address), Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Navigate to the {@link NotesFragment} for displaying important notes about the app
     */
    private void navigateToNotesFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction
                // Animations. this has to be before fragmentTransaction.replace()
                .setCustomAnimations(
                        androidx.fragment.R.animator.fragment_fade_enter, // Enter animation
                        androidx.fragment.R.animator.fragment_fade_exit, // Exit animation
                        androidx.fragment.R.animator.fragment_close_enter, // Pop enter animation (when navigating back)
                        androidx.fragment.R.animator.fragment_fade_exit // Pop exit animation (when navigating back)
                )
                .replace(R.id.fragment_container_notes, new NotesFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}