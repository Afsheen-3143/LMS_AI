package com.afsheen.aiassistant.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.afsheen.aiassistant.dto.request.AssistantFaqRequest;
import com.afsheen.aiassistant.dto.response.AssistantFaqResponse;
import com.afsheen.aiassistant.entity.AssistantFaq;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AssistantFaqMapper {

    AssistantFaqResponse toResponse(AssistantFaq entity);

    List<AssistantFaqResponse> toResponseList(List<AssistantFaq> entities);

    AssistantFaq toEntity(AssistantFaqRequest request);
}
