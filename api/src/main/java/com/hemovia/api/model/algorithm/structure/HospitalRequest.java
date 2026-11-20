package com.hemovia.api.model.algorithm.structure;

import java.util.Objects;

public record HospitalRequest(String id, String hospitalName) {
    public HospitalRequest {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(hospitalName, "hospitalName");
    }
}
