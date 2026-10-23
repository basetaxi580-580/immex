package com.immex.backend.model;

public record CloseMonthlyStateResponse(CloseMonthlyStatus status, boolean canSign, boolean canDownloadAnexo24) {}
