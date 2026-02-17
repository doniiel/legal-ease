package kz.legeal.ease.backend.request;

import lombok.Getter;

@Getter
public class LawyerRegisterRequest {

    private String firstName;

    private String middleName;

    private String lastName;

    private String iin;

    private String licenseNum;

    private String email;

    private String phone;
}
