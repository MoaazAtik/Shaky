package com.thewhitewings.shaky.ui.util;

import android.widget.SeekBar;

/**
 * Abstract class for convenience when implementing
 * the {@link SeekBar.OnSeekBarChangeListener} interface.
 * <br>
 * It requires the implementation of the {@link #onProgressChanged(SeekBar, int, boolean)} only.
 */
public abstract class OnSeekBarChangeListenerImpl implements SeekBar.OnSeekBarChangeListener {

     // Called when the progress of the SeekBar changes.
     // To be implemented when instantiating this class
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}
}