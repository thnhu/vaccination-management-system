package Vaccination.Management.System.advisor.service;

import Vaccination.Management.System.model.dto.advisor.AdvisorChatRequest;
import Vaccination.Management.System.model.dto.advisor.AdvisorChatResponse;

public interface CitizenAdvisorService {
    AdvisorChatResponse chat(Long citizenId, AdvisorChatRequest request);
}
