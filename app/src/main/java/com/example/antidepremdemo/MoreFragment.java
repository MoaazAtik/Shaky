package com.example.antidepremdemo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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

public class MoreFragment extends Fragment {

    private static final String TAG = "MoreFragment";
    private Button btnTone;
    private Button btnFeedback;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_more, container, false);

        btnTone = (Button) view.findViewById(R.id.btn_tone);
        btnFeedback = (Button) view.findViewById(R.id.btn_feedback);

        //btnTone
        btnTone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: btnTone");

                int rawResourceId = R.raw.soft;
                String rawResourceString = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        getResources().getResourcePackageName(rawResourceId) + '/' +
                        getResources().getResourceTypeName(rawResourceId) + '/' +
                        getResources().getResourceEntryName(rawResourceId);
                Uri rawResourceUri = Uri.parse(rawResourceString);

                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);//to choose from internal (ringtones) storage
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM | RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, rawResourceUri);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);

//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI); //to choose from external storage

                mGetContent.launch(intent);

            }//onClick
        });//onClickListener

        //btnFeedback
        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: btnFeedback");
                sendEmail();
            }//onClick
        });//btnFeedback OnClickListener

        return view;
    }//onCreateView


    //ActivityResultLauncher mGetContent
    private final ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

//                        Uri selectedToneUri = result.getData().getData();//to get tone selected from external storage
                        Uri selectedToneUri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);//to get tone selected from internal storage

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        if (selectedToneUri != null) {
                            sharedPreferences.edit().putString("alarm_tone", selectedToneUri.toString()).apply();
                        } else {
                            String previousToneStr = sharedPreferences.getString("alarm_tone", null);
                            sharedPreferences.edit().putString("alarm_tone", previousToneStr).apply();
                        }
                        Log.d(TAG, "onActivityResult: " + selectedToneUri);
                        Log.d(TAG, "onActivityResult: " + sharedPreferences.getString("alarm_tone", null));
                    }
                }//onActivityResult
            });//mGetContent

    //sendEmail
    private void sendEmail() {
        String recipientEmail = getString(R.string.recipient_email_address);
        String emailSubject = getString(R.string.email_subject);
        String emailBody = getString(R.string.email_body);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        if (emailIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(Intent.createChooser(emailIntent, "Send Feedback"));
        } else {
            Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), "Your feedback is welcome ", Toast.LENGTH_SHORT).show();
        }
    }//sendEmail()

}
