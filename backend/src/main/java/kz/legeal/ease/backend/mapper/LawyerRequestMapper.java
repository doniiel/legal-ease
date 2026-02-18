package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.LawyerApplication;
import kz.legeal.ease.backend.dto.LawyerRequestDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LawyerRequestMapper {

    LawyerRequestDto toDto(LawyerApplication e);
}
