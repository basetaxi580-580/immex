package com.immex.backend.model;

import java.util.List;

public record ConciliacionResult(int totalEntradas, int duplicados, int faltantes, int diferencias, int excepcionesGeneradas, List<ConciliacionExceptionItem> excepciones) {}