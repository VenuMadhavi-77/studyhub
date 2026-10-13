package com.studyhub.platform.modules.resource.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReliabilityFormulaTest {

    @Test
    @DisplayName("Reliability score with 0 reviews on unverified resource returns 0.0")
    void testZeroReviewsUnverified() {
        double score = ReviewService.computeReliabilityScore(0, 0.0, false, 0);
        assertEquals(0.0, score, 0.01);

        String label = ReviewService.computeReliabilityLabel(0, score);
        assertEquals("New - not enough reviews", label);
    }

    @Test
    @DisplayName("Reliability score with 0 reviews on verified resource returns verified baseline")
    void testZeroReviewsVerified() {
        double score = ReviewService.computeReliabilityScore(0, 0.0, true, 0);
        assertEquals(3.8, score, 0.01);

        String label = ReviewService.computeReliabilityLabel(0, score);
        assertEquals("New - not enough reviews", label);
    }

    @Test
    @DisplayName("Reliability score with 1 review is pulled towards prior mean C=3.5")
    void testSingleFiveStarReviewRegressesTowardsPrior() {
        // 1 review of 5.0: Bayesian = (1*5.0 + 5*3.5)/(1 + 5) = (5 + 17.5)/6 = 22.5/6 = 3.75
        double score = ReviewService.computeReliabilityScore(1, 5.0, false, 0);
        assertEquals(3.75, score, 0.01);

        String label = ReviewService.computeReliabilityLabel(1, score);
        assertEquals("New - not enough reviews", label);
    }

    @Test
    @DisplayName("Reliability score with many reviews converges towards true sample mean")
    void testManyReviewsConverge() {
        // 50 reviews of 4.8: Bayesian = (50*4.8 + 5*3.5)/(50 + 5) = (240 + 17.5)/55 = 257.5/55 ≈ 4.68
        // with verified bonus +0.3 = 4.98
        double score = ReviewService.computeReliabilityScore(50, 4.8, true, 0);
        assertEquals(4.98, score, 0.02);

        String label = ReviewService.computeReliabilityLabel(50, score);
        assertEquals("Highly reliable", label);
    }

    @Test
    @DisplayName("Reliability penalty for open reports reduces score appropriately")
    void testOpenReportPenalty() {
        // 10 reviews of 4.0: Bayesian = (10*4.0 + 5*3.5)/(10 + 5) = (40 + 17.5)/15 = 57.5/15 ≈ 3.83
        // 2 open reports -> penalty = 2 * 0.5 = 1.0 -> score = 2.83
        double score = ReviewService.computeReliabilityScore(10, 4.0, false, 2);
        assertEquals(2.83, score, 0.02);

        String label = ReviewService.computeReliabilityLabel(10, score);
        assertEquals("Mixed", label);
    }
}
