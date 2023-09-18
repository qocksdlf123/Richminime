package com.richminime.global.common.codef.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuccessDto {

    private String code;

    private String message;

    private String countryCode;

    private String clientType;

    private String organization;

    private String businessType;

}
