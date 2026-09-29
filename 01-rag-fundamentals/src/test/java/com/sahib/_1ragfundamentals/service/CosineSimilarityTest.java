package com.sahib._1ragfundamentals.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CosineSimilarityTest {
    @Test
    void testBetween_IdenticalVectors_ReturnsOne() {
        // Given
        float[] vector = {3, 4};
        // When
        double score = CosineSimilarity.between(vector, vector);
        // Then
        assertEquals(1, score, 0.000001);
    }

    @Test
    void testBetween_OppositeVectors_ReturnsMinusOne() {
        // Given
        float[] left = {1, 2};
        // When
        double score = CosineSimilarity.between(left, new float[]{-1, -2});
        // Then
        assertEquals(-1, score, 0.000001);
    }

    @Test
    void testBetween_OrthogonalVectors_ReturnsZero() {
        // Given
        float[] left = {1, 0};
        // When
        double score = CosineSimilarity.between(left, new float[]{0, 1});
        // Then
        assertEquals(0, score, 0.000001);
    }

    @Test
    void testBetween_MismatchedDimensions_Throws() {
        // Given
        float[] left = {1};
        // When
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> CosineSimilarity.between(left, new float[]{1, 2}));
        // Then
        assertEquals("Vector dimensions must match", error.getMessage());
    }

    @Test
    void testBetween_ZeroVector_ReturnsZero() {
        // Given
        float[] zero = {0, 0};
        // When
        double score = CosineSimilarity.between(zero, new float[]{1, 2});
        // Then
        assertEquals(0, score);
    }
}
