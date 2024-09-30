package com.example.revolution;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;


/**
 * Utility class for providing static methods used in classes for this application
 *
 * @author Sam Kapp
 */
public class ActivityUtils {

    // keys used to preserve the game state across config changes
    private static final String ROWS = "rows";
    private static final String COLS = "cols";
    private static final String NUM_MOVES = "moves";
    private static final String MOVES = "move";

    /**
     * Sets the Buttons in the specified tableLayout
     */
    public static void setImageButtons(Revolution game, Context context, TableLayout tableLayout,
                                       Button[] buttons) {
        int rows = game.getRows();
        int cols = game.getCols();
        tableLayout.removeAllViews();

        // Calculate size of each button to take up 90% of the width/height of the screen
        // depending on which is smaller so it fits better in different orientations
        int displayWidth = context.getResources().getDisplayMetrics().widthPixels;
        int displayHeight = context.getResources().getDisplayMetrics().heightPixels;
        int buttonSize = Math.min((5 * displayWidth / 10) / cols, (5 * displayHeight / 10) / rows);

        // Create each button and add to table layout
        for (int i = 0; i < rows; i++) {
            // Create a new row
            TableRow tableRow = new TableRow(context);

            for (int j = 0; j < cols; j++) {
                int index = i * cols + j;

                //Set the rows params
                TableRow.LayoutParams params = new TableRow.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                params.setMargins(2, 2, 2, 2);

                // create and set the buttons background and params
                buttons[index] = new Button(context);
                buttons[index].setBackgroundColor(ContextCompat.getColor(context, R.color.button_background));
                buttons[index].setLayoutParams(params);
                buttons[index].setTag(index);
                buttons[index].setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonSize / 2f);

                tableRow.addView(buttons[index]);
            }
            // add row to the table
            tableLayout.addView(tableRow);
        }
    }

    /**
     * Fills ImageButtons to represent the game
     */
    public static void drawBoard(Revolution game, Button[] buttons) {
        int rows = game.getRows();
        int cols = game.getCols();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int cellValue = game.get(i, j);
                int index = i * cols + j;

                buttons[index].setText(String.valueOf(cellValue));
            }
        }
    }

    /**
     * Displays a custom dialog using a specified layout.
     */
    public static void showCustomDialog(Activity activity, int layoutId) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(layoutId, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.dialog_background);
        }
    }

    /**
     * Displays a given string in a custom toast.
     */
    public static void showCustomToast(Activity activity, String message) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                activity.findViewById(R.id.toast_layout_root));

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * Write the state of a game to handle
     */
    public static void saveGame(Revolution game, Bundle bundle) {
        // Save the rows and columns
        int r = game.getRows();
        int c = game.getCols();
        bundle.putInt(ROWS, r);
        bundle.putInt(COLS, c);

        // Save the number of moves
        int numMoves = game.moves();
        bundle.putInt(NUM_MOVES, numMoves);

        // Create an array to hold the moves and add them to it
        int[][][] movesArray = new int[numMoves][][];
        for (int i = 0; i < numMoves; i++) {
            movesArray[i] = game.moveAt(i);
        }

        // Save the moves array in the Bundle
        bundle.putSerializable(MOVES, movesArray);
    }

    /**
     * instantiates the game from the saveGame bundle
     *
     * @return the savedGame
     */
    public static Revolution getSavedGame(Bundle bundle) {
        int rows = bundle.getInt(ROWS);
        int cols = bundle.getInt(COLS);
        int numMoves = bundle.getInt(NUM_MOVES);

        // Create new game and reset gridStates so they properly can be restored here
        // If don't reset states, the constructor will add a gridState automatically that isn't present
        // in the saved games data
        Revolution game = new Revolution(rows, cols, 0);
        game.resetGridStates();

        // Restore the moves
        int[][][] movesArray = (int[][][]) bundle.getSerializable(MOVES);
        if (movesArray != null) {
            for (int i = 0; i < numMoves; i++) {
                game.addMove(movesArray[i]);
            }
        }

        // Set the grid (current grid to be viewed) as the most recent moves
        if (numMoves > 0 && movesArray != null) {
            game.setGrid(movesArray[numMoves-1]);
        }

        return game;
    }

}
