package com.thewhitewings.shaky;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class MoreFragment extends Fragment {

    private static final String TAG = "MoreFragment";
    private Button btnTone;
    private Button btnFeedback;
    private Button btnNotes;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate a custom layout for the fragment
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        // Initialize the UI elements
        btnTone = (Button) view.findViewById(R.id.btn_tone);
        btnFeedback = (Button) view.findViewById(R.id.btn_feedback);
        btnNotes = (Button) view.findViewById(R.id.btn_notes);

        //btnTone
        btnTone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the Uri of the default tone
                int rawResourceId = R.raw.soft;
                String rawResourceString = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        getResources().getResourcePackageName(rawResourceId) + '/' +
                        getResources().getResourceTypeName(rawResourceId) + '/' +
                        getResources().getResourceEntryName(rawResourceId);
                Uri rawResourceUri = Uri.parse(rawResourceString);

                // Create an intent to open the ringtone picker to change the alarm tone of the app
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);//to choose from internal (ringtones) storage
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, rawResourceUri);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);

//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI); //to choose from external storage

                // Open the ringtone picker
                mGetContent.launch(intent);

            }//onClick
        });//onClickListener

        //btnFeedback
        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }//onClick
        });//btnFeedback OnClickListener

        //btnNotes
        btnNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Direct to NotesFragment
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
        });//btnNotes OnClickListener

        return view;
    }//onCreateView


    // ActivityResultLauncher mGetContent
    // Start the ringtone picker activity, and handle the picked tone
    private final ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                        // Get the Uri of the selected tone
//                        Uri selectedToneUri = result.getData().getData();//to get tone selected from external storage
                        Uri selectedToneUri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);//to get tone selected from internal storage

                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        // If a tone is picked, save it to the preferences under "alarm_tone"
                        if (selectedToneUri != null) {
                            sharedPreferences.edit().putString("alarm_tone", selectedToneUri.toString()).apply();
                        } else {
                            // If no tone is picked, save the previously selected tone under "alarm_tone" if one exists;
                            // otherwise save null (which results in playing the default tone after being handled by MediaAndSensorService.selectedTone())
                            String previousToneStr = sharedPreferences.getString("alarm_tone", null);
                            sharedPreferences.edit().putString("alarm_tone", previousToneStr).apply();
                        }
                    }
                }//onActivityResult
            });//mGetContent

    // sendEmail
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
            Toast.makeText(requireActivity(), getString(R.string.your_feedback_is_welcome_at) + "\n" + getString(R.string.recipient_email_address), Toast.LENGTH_LONG).show();
        }
    }//sendEmail()

}
