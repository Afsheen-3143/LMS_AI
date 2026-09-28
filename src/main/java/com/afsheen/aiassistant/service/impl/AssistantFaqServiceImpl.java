package com.afsheen.aiassistant.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.afsheen.aiassistant.dto.request.AssistantFaqRequest;
import com.afsheen.aiassistant.dto.response.AssistantFaqResponse;
import com.afsheen.aiassistant.mapper.AssistantFaqMapper;
import com.afsheen.aiassistant.service.AssistantFaqService;
import com.afsheen.aiassistant.entity.AssistantFaq;
import com.afsheen.aiassistant.exceptions.ResourceNotFoundException;
import com.afsheen.aiassistant.repository.AssistantFaqRepository;

@Service
public class AssistantFaqServiceImpl implements AssistantFaqService {

    private final AssistantFaqRepository assistantFaqRepository;
    private final AssistantFaqMapper assistantFaqMapper;

    public AssistantFaqServiceImpl(
            AssistantFaqRepository assistantFaqRepository,
            AssistantFaqMapper assistantFaqMapper) {
        this.assistantFaqRepository = assistantFaqRepository;
        this.assistantFaqMapper = assistantFaqMapper;
    }

    @Override
    public AssistantFaqResponse createFaq(AssistantFaqRequest request) {
        AssistantFaq faq = assistantFaqMapper.toEntity(request);
        return assistantFaqMapper.toResponse(assistantFaqRepository.save(faq));
    }

    @Override
    public AssistantFaqResponse updateFaq(Long id, AssistantFaqRequest request) {
        AssistantFaq faq = assistantFaqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found with id: " + id));
        faq.setCategory(request.getCategory());
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        faq.setActive(request.isActive());
        return assistantFaqMapper.toResponse(assistantFaqRepository.save(faq));
    }

    @Override
    public void deleteFaq(Long id) {
        if (!assistantFaqRepository.existsById(id)) {
            throw new ResourceNotFoundException("FAQ not found with id: " + id);
        }
        assistantFaqRepository.deleteById(id);
    }

    @Override
    public List<AssistantFaqResponse> getAllFaqs() {
        return assistantFaqMapper.toResponseList(assistantFaqRepository.findAll());
    }
}
