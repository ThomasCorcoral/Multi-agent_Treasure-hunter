package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class HungarianAlgo {

    int[][] matrix;
    int[] squareInRow, squareInCol, rowIsCovered, colIsCovered, staredZeroesInRow;
    public HungarianAlgo(int[][] matrix) {
        if (matrix.length != matrix[0].length) {
            try {
                throw new IllegalAccessException("The matrix is not square!");
            } catch (IllegalAccessException ex) {
                System.err.println(ex);
                System.exit(1);
            }
        }

        this.matrix = matrix;
        squareInRow = new int[matrix.length];
        squareInCol = new int[matrix[0].length];
        rowIsCovered = new int[matrix.length];
        colIsCovered = new int[matrix[0].length];
        staredZeroesInRow = new int[matrix.length];
        Arrays.fill(staredZeroesInRow, -1);
        Arrays.fill(squareInRow, -1);
        Arrays.fill(squareInCol, -1);
    }

    public int[][] findOptimalAssignment() {
        step1();   
        step2();    
        step3();

        while (!allColumnsAreCovered()) {
            int[] mainZero = step4();
            while (mainZero == null) {
                step7();
                mainZero = step4();
            }
            if (squareInRow[mainZero[0]] == -1) {
                step6(mainZero);
                step3();
            } else {
                rowIsCovered[mainZero[0]] = 1;
                colIsCovered[squareInRow[mainZero[0]]] = 0;
                step7();
            }
        }

        int[][] optimalAssignment = new int[matrix.length][];
        for (int i = 0; i < squareInCol.length; i++) {
            optimalAssignment[i] = new int[]{i, squareInCol[i]};
        }
        return optimalAssignment;
    }

    private boolean allColumnsAreCovered() {
        for (int i : colIsCovered) {
            if (i == 0) {
                return false;
            }
        }
        return true;
    }

    private void step1() {
        // rows
        for (int i = 0; i < matrix.length; i++) {
            int currentRowMin = Integer.MAX_VALUE;
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] < currentRowMin) {
                    currentRowMin = matrix[i][j];
                }
            }
            for (int k = 0; k < matrix[i].length; k++) {
                matrix[i][k] -= currentRowMin;
            }
        }

        for (int i = 0; i < matrix[0].length; i++) {
            int currentColMin = Integer.MAX_VALUE;
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[j][i] < currentColMin) {
                    currentColMin = matrix[j][i];
                }
            }
            for (int k = 0; k < matrix.length; k++) {
                matrix[k][i] -= currentColMin;
            }
        }
    }

    private void step2() {
        int[] rowHasSquare = new int[matrix.length];
        int[] colHasSquare = new int[matrix[0].length];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[i][j] == 0 && rowHasSquare[i] == 0 && colHasSquare[j] == 0) {
                    rowHasSquare[i] = 1;
                    colHasSquare[j] = 1;
                    squareInRow[i] = j;
                    squareInCol[j] = i;
                    continue;
                }
            }
        }
    }

    private void step3() {
        for (int i = 0; i < squareInCol.length; i++) {
            colIsCovered[i] = squareInCol[i] != -1 ? 1 : 0;
        }
    }

    private void step7() {
        int minUncoveredValue = Integer.MAX_VALUE;
        for (int i = 0; i < matrix.length; i++) {
            if (rowIsCovered[i] == 1) {
                continue;
            }
            for (int j = 0; j < matrix[0].length; j++) {
                if (colIsCovered[j] == 0 && matrix[i][j] < minUncoveredValue) {
                    minUncoveredValue = matrix[i][j];
                }
            }
        }

        if (minUncoveredValue > 0) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[0].length; j++) {
                    if (rowIsCovered[i] == 1 && colIsCovered[j] == 1) {
                        matrix[i][j] += minUncoveredValue;
                    } else if (rowIsCovered[i] == 0 && colIsCovered[j] == 0) {
                        matrix[i][j] -= minUncoveredValue;
                    }
                }
            }
        }
    }

    private int[] step4() {
        for (int i = 0; i < matrix.length; i++) {
            if (rowIsCovered[i] == 0) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (matrix[i][j] == 0 && colIsCovered[j] == 0) {
                        staredZeroesInRow[i] = j;
                        return new int[]{i, j};
                    }
                }
            }
        }
        return null;
    }

    private void step6(int[] mainZero) {
        int i = mainZero[0];
        int j = mainZero[1];

        Set<int[]> K = new LinkedHashSet<>();
        K.add(mainZero);
        boolean found = false;
        do {
            if (squareInCol[j] != -1) {
                K.add(new int[]{squareInCol[j], j});
                found = true;
            } else {
                found = false;
            }
            if (!found) {
                break;
            }
            i = squareInCol[j];
            j = staredZeroesInRow[i];
            if (j != -1) {
                K.add(new int[]{i, j});
                found = true;
            } else {
                found = false;
            }

        } while (found);
        for (int[] zero : K) {
            if (squareInCol[zero[1]] == zero[0]) {
                squareInCol[zero[1]] = -1;
                squareInRow[zero[0]] = -1;
            }
            if (staredZeroesInRow[zero[0]] == zero[1]) {
                squareInRow[zero[0]] = zero[1];
                squareInCol[zero[1]] = zero[0];
            }
        }
        Arrays.fill(staredZeroesInRow, -1);
        Arrays.fill(rowIsCovered, 0);
        Arrays.fill(colIsCovered, 0);
    }
}
