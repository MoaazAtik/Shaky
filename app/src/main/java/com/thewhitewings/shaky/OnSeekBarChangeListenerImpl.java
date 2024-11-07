package com.thewhitewings.shaky;

import android.widget.SeekBar;

public abstract class OnSeekBarChangeListenerImpl implements SeekBar.OnSeekBarChangeListener {

     // Called when the progress of the SeekBar changes.
     // To be implemented when instantiating this class
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}
}
