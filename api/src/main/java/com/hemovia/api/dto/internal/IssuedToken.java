package com.hemovia.api.dto.internal;

import java.time.Instant;

/*
Um token recém-emitido e o instante em que ele deixa de valer.
*/
public record IssuedToken(String value, Instant expiresAt) { }
