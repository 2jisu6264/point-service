package com.musinsa.sys.common.exception;

import lombok.Getter;

@Getter
public class ServiceException extends Exception {

    private static final long serialVersionUID = 4577337814375765407L;

    private String procCd;

    private String apnMsg;

    public ServiceException(String inProcCd) {
        this.procCd = inProcCd;
    }

    public ServiceException(String inProcCd, String apnMsg) {
        this.procCd = inProcCd;
        this.apnMsg = apnMsg;
    }
}
