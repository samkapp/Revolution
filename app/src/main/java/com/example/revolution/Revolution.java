package com.example.revolution;

import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Game of Revolution
 *
 * @author Sam Kapp
 */

public class Revolution {
    private int[][] grid;
    private Stack<int[][]> gridStates;

    private final int rows;
    private final int cols;

    /**
     *Default Constructor
     */
    public Revolution() {
        this(3, 3, 3);
    }

    /**
     * Constructor with solDepth
     */
    public Revolution(int solDepth) {
        this(3, 3, solDepth);
    }

    /**
     * Explicit Constructor
     */
    public Revolution(int rows, int cols, int solDepth) {
        this.rows = rows;
        this.cols = cols;

        gridStates = new Stack<>();
        gridInit(rows, cols, solDepth);

        // Reset gridStates and add the current grid to it, so that the gridStates from
        // random rotations aren't added
        this.resetGridStates();
        addMove((this.grid));
    }

    /**
     * Create the grid with the given rows and cols
     * Set the game up through a number of random rotations given by solDepth
     */
    private void gridInit(int rows, int cols, int solDepth) {
        grid = new int[rows][cols];

        // Fill the grid in winning position
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = i*cols + j + 1;
            }
        }

        // Randomly rotate the grid solDepth times
        for (int i = 0; i < solDepth; i++) {
            randomRotation();
        }
    }

    /**
     * chooses an anchor position and direction (left or right)
     * at random and then rotates the corresponding 2x2 subgrid
     *
     * Anchor positions are all tiles except tiles on the last row and col
     */
    private void randomRotation() {
        // get random valid anchor point
        int row = ThreadLocalRandom.current().nextInt(rows - 1);
        int col = ThreadLocalRandom.current().nextInt(cols - 1);

        // get right or left movement (right = true, left = false)
        boolean right = ThreadLocalRandom.current().nextInt(2) == 0;

        if (right) {
            rotateRight(row, col);
        } else {
            rotateLeft(row, col);
        }
    }

    /**
     * Given an anchor point, checks if it is a valid rotation, then rotates right
     */
    public void rotateRight(int row, int col) {
        if (!isValidAnchor(row, col)) return;

        int temp = grid[row][col];

        grid[row][col] = grid[row+1][col];
        grid[row+1][col] = grid[row+1][col+1];
        grid[row+1][col+1] = grid[row][col+1];
        grid[row][col+1] = temp;

        gridStates.push(copyGrid(grid));

    }

    /**
     * Given an anchor point, checks if it is a valid rotation, then rotates left
     */
    public void rotateLeft(int row, int col) {
        if (!isValidAnchor(row, col)) return;

        // Use one temporary variable
        int temp = grid[row][col];

        grid[row][col] = grid[row][col + 1]; // topLeft <- topRight
        grid[row][col + 1] = grid[row + 1][col + 1]; // topRight <- bottomRight
        grid[row + 1][col + 1] = grid[row + 1][col]; // bottomRight <- bottomLeft
        grid[row + 1][col] = temp; // bottomLeft <- temp (original topLeft)

        gridStates.push(copyGrid(grid));

    }

    /**
     * Checks if the given rows and cols are a valid anchor point 
     * 
     * @return true if a valid anchor, false otherwise
     */
    public boolean isValidAnchor(int row, int col) {
        // an anchor point is valid if it is not in the final row or col
        return (row < rows -1 && col < cols - 1);
    }

    /**
     * @return true if the game is solved, false otherwise
     */
    public boolean isOver() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] != (i*cols + j + 1))
                    return false;
            }
        }
        return true;
    }

    /**
     * Undoes the last move made, if there is a move to undo
     *
     * @return true if a move was undone, and false otherwise
     */
    public boolean undo() {
        if (!gridStates.empty()) {
            this.grid = gridStates.pop();
            return true;
        }
        return false;
    }

    /**
     * adds a move to the grid state
     * for use only in saving/restoring the game state
     */
    public void addMove(int[][] move) {
        gridStates.push(move);
    }

    /**
     * returns the grid state from a given index
     */
    public int[][] moveAt(int index) {
        return gridStates.get(index);
    }

    /**
     * @return the number of moves made
     */
    public int moves() {
        return gridStates.size();
    }


    /* Row and Col getters */
    public int getRows() { return rows; }
    public int getCols() { return cols; }

    /**
     * @return the value at the given index
     */
    public int get(int row, int col) {
        return grid[row][col];
    }

    /**
     * sets the grid to the given 2d-array state
     */
    public void setGrid(int[][] state) {
        // Create a new grid with the same dimensions
        this.grid = new int[rows][cols];

        // Perform a deep copy of the state
        for (int i = 0; i < rows; i++) {
            System.arraycopy(state[i], 0, this.grid[i], 0, cols);
        }
    }

    /**
     * Returns a copy of a grid state
     */
    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, cols);
        }
        return copy;
    }

    /**
     * Clears the gridStates history
     */
    public void resetGridStates() {
        this.gridStates = new Stack<>();
    }

}

