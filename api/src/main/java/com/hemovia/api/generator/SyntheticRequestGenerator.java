package com.hemovia.api.generator;

import com.hemovia.api.model.SyntheticRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Component
public class SyntheticRequestGenerator {

    private static final long SEED = 42L;
    private static final int HOSPITAL_COUNT = 500;
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY_EXCLUSIVE = 50;

    public List<SyntheticRequest> generate(int quantity) {
        Random random = new Random(SEED);

        return IntStream.range(0, quantity)
                .mapToObj(index -> new SyntheticRequest(
                        "HOSP-" + random.nextInt(HOSPITAL_COUNT),
                        random.nextInt(MIN_QUANTITY, MAX_QUANTITY_EXCLUSIVE)
                ))
                .toList();
    }
}
