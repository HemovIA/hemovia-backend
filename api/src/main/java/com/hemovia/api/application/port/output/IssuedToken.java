package com.hemovia.api.application.port.output;

import java.time.Instant;

/*
Um token recém-emitido e o instante em que ele deixa de valer.
*/
public record IssuedToken(String value, Instant expiresAt) { }
