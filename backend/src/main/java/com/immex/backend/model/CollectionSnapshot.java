package com.immex.backend.model;

import java.util.List;

public record CollectionSnapshot(String name, long count, List<String> sample) {}
