package Vaccination.Management.System.service;

import Vaccination.Management.System.model.dto.advisor.RecommendationResponse;

import java.util.List;

public interface RecommendationService {
    List<RecommendationResponse> getRecommendations(Long citizenId);
}
