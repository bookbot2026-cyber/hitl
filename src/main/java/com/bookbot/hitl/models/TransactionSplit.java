package com.bookbot.hitl.models;

import java.io.Serializable;
import java.math.BigDecimal;

public record TransactionSplit(String person, BigDecimal amount) implements Serializable {
    private static final long serialVersionUID = 1L;
}
