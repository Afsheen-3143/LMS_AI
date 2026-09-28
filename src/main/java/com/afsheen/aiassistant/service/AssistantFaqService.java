package com.afsheen.aiassistant.service;

import java.util.List;

import com.afsheen.aiassistant.dto.request.AssistantFaqRequest;
import com.afsheen.aiassistant.dto.response.AssistantFaqResponse;

public interface AssistantFaqService {

    AssistantFaqResponse createFaq(AssistantFaqRequest request);

    AssistantFaqResponse updateFaq(Long id, AssistantFaqRequest request);

    void deleteFaq(Long id);

    List<AssistantFaqResponse> getAllFaqs();
}
