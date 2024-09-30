package com.example.revolution;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

/**
 * Provides game-related sound effects
 */
public class SoundManager {
    private SoundPool soundPool;
    private final int startSound;  // start/restarting a new game
    private final int failSound;   // invalid undo / anchor choice
    private final int undoSound;   // take back a move
    private final int winSound;    // puzzle solved
    private final int moveSound; // successful rotation
    /**
     * Initializes a new sound manager for a given context.
     */
    public SoundManager(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder().setMaxStreams(1)
                .setAudioAttributes(audioAttributes).build();

        startSound = soundPool.load(context, R.raw.start, 1);
        failSound = soundPool.load(context, R.raw.fail, 1);
        winSound = soundPool.load(context, R.raw.win, 1);
        undoSound = soundPool.load(context, R.raw.undo, 1);
        moveSound = soundPool.load(context, R.raw.move, 1);
    }

    /**
     * Releases all memory and resources used by the SoundPool.
     */
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    public void playStartSound() {
        play(startSound);
    }

    public void playMoveSound() {
        play(moveSound);
    }

    public void playWinSound() {
        play(winSound);
    }

    public void playFailSound() {
        play(failSound);
    }

    public void playUndoSound() {
        play(undoSound);
    }

    /**
     * Plays a sound specified by its resource ID.
     */
    private void play(int id) {
        if (soundPool != null) {
            soundPool.play(id, 1, 1, 0, 0, 1);
        }
    }
}