package com.musinsa.sys.common.dto;

import com.musinsa.sys.common.ProcessCode;

import java.util.Map;
import java.util.Optional;

public class ResultProcess {
    public static <T> ProcessResult convertTo(T response){
        ProcessCode processCode = ProcessCode.findByProcessCode("HCO000");
        ProcessResult<T> processResult = new ProcessResult<T>(
                processCode.getSucsFalr(),
                processCode.getProcCd(),
                processCode.getRsltMesg(),
                response);
        return processResult;
    }
    public static String getValueAsString(Map<String, Object> map, String key) {
        if (map == null) {
            return "0";
        }
        return Optional.ofNullable(map.get(key))
                .map(Object::toString)
                .orElse("0");
    }

}
