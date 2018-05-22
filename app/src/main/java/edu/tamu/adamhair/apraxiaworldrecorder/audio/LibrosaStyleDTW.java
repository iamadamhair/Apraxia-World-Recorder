package edu.tamu.adamhair.apraxiaworldrecorder.audio;

import java.util.ArrayList;
import java.util.List;

public class LibrosaStyleDTW {
    private int[][] warpingPath;
    private double[][] costMatrix;
    private double[][] distanceMatrix;
    private int[][] directionMatrix;
    private double[][] template1;
    private double[][] template2;
    private int[][] directionOptions = {{1,0}, {1,1}, {0,1}}; // Indices corresponding to the three directions to check


    public LibrosaStyleDTW(double[][] template1, double[][] template2) {
        // Always put the shorter template into the private template1
        if (template1.length < template2.length) {
            this.template1 = template1;
            this.template2 = template2;
        } else {
            this.template1 = template2;
            this.template2 = template1;
        }

        this.costMatrix = new double[this.template1.length][this.template2.length];
        // The distance matrix is one row and column larger so we can look backwards from 1,1
        this.distanceMatrix = new double[this.template1.length+1][this.template2.length+1];
        this.directionMatrix = new int[this.template1.length+1][this.template2.length+1];

        /* Fill in cost matrix by comparing all vector combinations */
        for (int i = 0; i < this.template1.length; i++) {
            for (int j = 0; j < this.template2.length; j++) {
                this.costMatrix[i][j] = distanceBetweenVectors(this.template1[i], this.template2[j]);
            }
        }

        /* Initialize the distance matrix to infinity and the direction matrix to -1 */
        for (int i = 0; i < this.distanceMatrix.length; i++) {
            for (int j = 0; j < this.distanceMatrix[0].length; j++) {
                this.distanceMatrix[i][j] = Double.POSITIVE_INFINITY;
                this.directionMatrix[i][j] = -1;
            }
        }

        /* Populate most of the second row of the distance matrix with the cost matrix.
        *  This is so when we look backwards from the second row to the first, the second
        *  will be the smaller values (looking back on inf is larger than current val) */
        for (int i = 1; i < this.distanceMatrix[0].length; i++) {
            this.distanceMatrix[1][i] = costMatrix[0][i - 1];
        }

        /* Fill in the distance matrix */
        double current_distance = 0;
        for (int i = 1; i < this.distanceMatrix.length; i++) {
            for (int j = 1; j < this.distanceMatrix[0].length; j++) {

                /* Check left, right, and diagonal for best value */
                for (int k = 0; k < this.directionOptions.length; k++) {
                    current_distance = this.distanceMatrix[i - this.directionOptions[k][0]][j - this.directionOptions[k][1]] + this.costMatrix[i - 1][j - 1];
                    if (current_distance < this.distanceMatrix[i][j]) {
                        this.distanceMatrix[i][j] = current_distance;
                        this.directionMatrix[i][j] = k;
                    }
                }
            }
        }

        /* Find minimum index of the last row in the distance matrix */
        int minIdx = 0;
        double minVal = Double.POSITIVE_INFINITY;
        for (int i = 0; i < this.distanceMatrix.length; i++) {
            if (this.distanceMatrix[this.template1.length][i] < minVal) {
                minIdx = i;
                minVal = this.distanceMatrix[this.template1.length][i];
            }
        }

        /* Find the path through the matrix */
        int[] currentIdx = {this.template1.length - 1, minIdx};
        List<int[]> path = new ArrayList<>();
        int[] firstPair = {currentIdx[0], currentIdx[1]};
        path.add(firstPair);

        /* Don't need to find 0,0, just need to make it to the first row, which is 1 since 0 is inf */
        int[] delta;
        while (currentIdx[0] > 1) {
            delta = this.directionOptions[this.directionMatrix[currentIdx[0]][currentIdx[1]]];
            currentIdx[0] = currentIdx[0] - delta[0];
            currentIdx[1] = currentIdx[1] - delta[1];
            int[] touple = {currentIdx[0], currentIdx[1]};
            path.add(touple);
        }

        /* Reverse the path */
        warpingPath = new int[path.size()][2];
        int pathIdx = 0;
        for (int i = path.size() - 1; i >= 0; i--) {
            warpingPath[pathIdx][0] = path.get(i)[0];
            warpingPath[pathIdx][1] = path.get(i)[1];
            pathIdx++;
        }
    }


    public int[][] getWarpingPath() {
        return warpingPath;
    }


    public double computeRMSE() {
        double distance = 0;
        for (int i = 0; i < this.warpingPath.length; i++) {
            for (int j = 0; j < this.template1[0].length; j++) {
                distance += Math.pow(this.template1[warpingPath[i][0]][j] - this.template2[warpingPath[i][1]][j], 2);
            }
        }
        return Math.sqrt(distance)/warpingPath.length;
    }

    private double distanceBetweenVectors(double[] vector1, double[] vector2) {
        /* Compute the Euclidean distance between two vectors */
        double distance = 0;
        for (int i = 0; i < vector1.length; i++) {
            distance += (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
        }
        return Math.sqrt(distance);
    }

}
