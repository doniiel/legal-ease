package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.LawyerApplication;
import kz.legeal.ease.backend.dto.LawyerApplicationDto;
import kz.legeal.ease.backend.dto.LawyerApplicationPreviewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface LawyerApplicationMapper {

    @Mapping(source = "user", target = "lawyerInfo")
    @Mapping(source = "reviewer", target = "reviewerInfo")
    LawyerApplicationDto toDto(LawyerApplication e);

    @Mapping(source = "user", target = "lawyerInfo")
    LawyerApplicationPreviewDto toPreviewDto(LawyerApplication e);
}
