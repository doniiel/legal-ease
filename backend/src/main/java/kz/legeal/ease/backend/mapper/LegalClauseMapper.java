package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.LegalClause;
import kz.legeal.ease.backend.dto.LegalClauseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LegalClauseMapper {

    @Mapping(source = "category.id",   target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    LegalClauseDto toDto(LegalClause clause);
}
