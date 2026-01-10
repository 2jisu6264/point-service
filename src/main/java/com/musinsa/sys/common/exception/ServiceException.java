package com.musinsa.sys.common.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    private final String procCd;

    private String apnMsg;

    public ServiceException(String inProcCd) {
        this.procCd = inProcCd;
    }

    public ServiceException(String inProcCd, String apnMsg) {
        this.procCd = inProcCd;
        this.apnMsg = apnMsg;
    }
}
