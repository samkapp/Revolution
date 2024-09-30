package com.example.revolution;

import android.os.Bundle;

import com.google.android.material.appbar.MaterialToolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TableLayout;

/**
 * Plays the game of Revolution
 * Game plays in a 3x3 grid with a default solDepth set to 3
 *
 * @author Sam Kapp
 */
public class MainActivity extends AppCompatActivity {

    // constants for the game and numberPicker
    private static final int initRowCount = 3;
    private static final int initColCount = 3;
    private static final int initSolDepth = 3;
    private static final int solDepthMin = 1;
    private static final int solDepthMax = 50;

    // Values needed for the game
    private Revolution game = new Revolution(initRowCount, initColCount, initSolDepth);
    private final int[] anchor = {initRowCount, initColCount};
    private int numberPickerValue;

    // Layout values
    private TableLayout tableLayout; // contains ImageButtons
    private Button[] buttons; // displays the number tiles
    private SoundManager soundManager;  // for sound effects

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tableLayout = findViewById(R.id.mainTableLayout);
        soundManager = new SoundManager(this);

        if (savedInstanceState != null) {
            game = ActivityUtils.getSavedGame(savedInstanceState);
        }

        // setup grid buttons, numberPicker, and draw the board
        setNumberPicker();
        setImageButtons();
        drawBoard();

        // Setup linear layout buttons
        findViewById(R.id.mainRestartButton).setOnClickListener(this::restart);
        findViewById(R.id.rotateLeftButton).setOnClickListener(move(false));
        findViewById(R.id.rotateRightButton).setOnClickListener(move(true));
        findViewById(R.id.mainUndoButton).setOnClickListener(this::undo);
    }

    /**
     * Draws the board
     */
    private void drawBoard() { ActivityUtils.drawBoard(game, buttons);}

    /**
     * Sets the image buttons for the Revolution grid and their event handlers
     */
    private void setImageButtons() {
        int r = game.getRows();
        int c = game.getCols();
        buttons = new Button[r * c];
        ActivityUtils.setImageButtons(game, this, tableLayout, buttons);

        // set the event handler
        for (Button button : buttons) {
            button.setOnClickListener(this::setAnchor);
        }
    }

    /**
     * Sets the number picker, and the eventListener
     */
    private void setNumberPicker() {
        // contains the numberPicker for solDepth
        NumberPicker numberPicker = findViewById(R.id.mainNumberPicker);
        numberPicker.setMinValue(solDepthMin);
        numberPicker.setMaxValue(solDepthMax);
        numberPicker.setValue(3);
        numberPickerValue = 3;

        // set the value to always be the newly changed to value
        numberPicker.setOnValueChangedListener((picker, oldValue, newValue) ->
                numberPickerValue = newValue);
    }

    /**
     * Checks the users click, and if a valid anchor highlights the sub grid
     * saving the anchor value for the players rotation
     */
    private void setAnchor(View view) {
        int tag = (int) view.getTag();
        int row = tag / game.getCols();
        int col = tag % game.getCols();

        // If the anchor is valid, save it, and highlight the subgrid
        if (game.isValidAnchor(row, col)) {
            anchor[0] = row;
            anchor[1] = col;

            // reset any highlights
            for (Button button : buttons) {
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.button_background));
            }

            // subgrid positions layout
            int[][] subgridPositions = {
                    {row, col},
                    {row, col + 1},
                    {row + 1, col},
                    {row + 1, col + 1}
            };

            // use subgrid positions to set needed highlights
            for (int[] pos : subgridPositions) {
                int subRow = pos[0];
                int subCol = pos[1];
                int index = subRow * game.getCols() + subCol;

                // access the sub grids button and highlight it
                Button button = buttons[index];
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.highlight_color));
            }
        } else {
            soundManager.playFailSound();
            showCustomToast(getString(R.string.anchor_fail));
        }
    }

    /**
     * Restarts the game, using the numberPickers value for the solDepth
     */
    private void restart(View view) {
        game = new Revolution(initRowCount, initColCount, numberPickerValue);
        setImageButtons();
        drawBoard();
        soundManager.playStartSound();
    }

    /**
     * Event handler for moving the tiles
     * @return an OnClickListener used for setting up the setOnClickListener of the rotate buttons
     */
    private View.OnClickListener move(boolean right) {
        return view -> {
            // Check that an anchor has been set before the rotate buttons are being used
            if (game.isValidAnchor(anchor[0], anchor[1])) {
                if (right) {
                    game.rotateRight(anchor[0], anchor[1]);
                } else {
                    game.rotateLeft(anchor[0], anchor[1]);
                }
                soundManager.playMoveSound();
                drawBoard();

                if (game.isOver()) {
                    showCustomToast("You have solved the puzzle!");
                    soundManager.playWinSound();
                }
            } else {
                soundManager.playFailSound();
                showCustomToast(getString(R.string.no_anchor_fail));
            }
        };
    }

    /**
     * Event handler for undo button
     */
    private void undo(View view) {
        if (game.undo()) {
            drawBoard();
            soundManager.playUndoSound();
        } else {
            soundManager.playFailSound();
            showCustomToast(getString(R.string.undo_fail));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Actions for menu items in options menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_about) {
            ActivityUtils.showCustomDialog(this, R.layout.dialog_about);
        }

        if (id == R.id.menu_exit) {
            showExitDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows an exit dialog asking if the user wants to exit (if so, terminates).
     */
    protected void showExitDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_exit, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (v, n) -> finish());
        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.dialog_background);
        }
    }

    /**
     * Displays a custom toast with a given message.
     */
    private void showCustomToast(String msg) {
        ActivityUtils.showCustomToast(this, msg);
    }

    /**
     * Releases memory and other resources used by the sound manager.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundManager.release();
    }

    /**
     * Saves the number of frogs and toads, the number of moves made, and the indices of
     * the moves.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ActivityUtils.saveGame(game, outState);
    }
}