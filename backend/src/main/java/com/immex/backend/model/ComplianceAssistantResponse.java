package com.immex.backend.model;

import java.util.List;

public record ComplianceAssistantResponse(
    String scope,
    String summary,
    List<ComplianceRecommendation> recommendations,
    List<String> facts,
    List<String> limitations) {}
