package com.hemovia.api.service;

import com.hemovia.api.model.StatisticsResult;
import com.hemovia.api.model.SyntheticRequest;

import java.util.List;

public interface SequentialStatsService {

    StatisticsResult calculateSequential(List<SyntheticRequest> requests);
}
