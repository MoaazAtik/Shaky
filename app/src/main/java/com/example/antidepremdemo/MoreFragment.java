package com.example.antidepremdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
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

//                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
//                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
//                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tonee");
//                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mGetAlarmToneUri());

                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);//to choose from internal (ringtones) storage
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM | RingtoneManager.TYPE_NOTIFICATION);
//                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
//                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tonee");

//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI); //to choose from external storage

                mGetContent.launch(intent);

            }//onClick
        });//onClickListener

        //btnFeedback
        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: btnFeedback");

                Toast.makeText(getContext(), " btnFeedback " + getContext(), Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "btnFeedback 2 " + getActivity(), Toast.LENGTH_SHORT).show();
            }
        });
//        return super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

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
//                        sharedPreferences.edit().putString("alarm_tone", null).apply();
                        if (selectedToneUri != null) {
                            sharedPreferences.edit().putString("alarm_tone", selectedToneUri.toString()).apply();
                        } else {
                            sharedPreferences.edit().putString("alarm_tone", null).apply();
                        }
                        Log.d(TAG, "onActivityResult: " + selectedToneUri);
                        Log.d(TAG, "onActivityResult: " + sharedPreferences.getString("alarm_tone", null));
                    }
                }//onActivityResult
            });//mGetContent

}
