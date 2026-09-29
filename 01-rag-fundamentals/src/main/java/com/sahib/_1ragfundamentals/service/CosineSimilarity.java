package com.sahib._1ragfundamentals.service;

public final class CosineSimilarity {
    private CosineSimilarity() {
    }

    public static double between(float[] left, float[] right) {
        if (left.length != right.length) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }
        double dot = 0;
        double leftNorm = 0;
        double rightNorm = 0;
        for (int i = 0; i < left.length; i++) {
            dot += (double) left[i] * right[i];
            leftNorm += (double) left[i] * left[i];
            rightNorm += (double) right[i] * right[i];
        }
        if (leftNorm == 0 || rightNorm == 0) {
            return 0;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }
}
