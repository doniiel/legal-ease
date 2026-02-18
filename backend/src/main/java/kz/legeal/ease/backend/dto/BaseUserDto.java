package kz.legeal.ease.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseUserDto {

    private Long id;

    private String email;

    private String fio;

    private String iin;

}
