package com.immex.backend.model;

public record ComplianceRecommendation(
    String problem,
    String cause,
    String impact,
    String suggestedAction,
    String evidence,
    boolean factBased) {}
