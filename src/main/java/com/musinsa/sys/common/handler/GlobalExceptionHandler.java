package com.musinsa.sys.common.handler;

import com.musinsa.sys.common.dto.ProcessResult;
import com.musinsa.sys.common.enums.ProcessCode;
import com.musinsa.sys.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    //@Valid를 통해 발견된 MethodArgumentNotValidException 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProcessResult<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return new ProcessResult<>(null, ProcessCode.HCO998.getProcCd());
    }

    // 404 Not Found 처리
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleNotFound(NoHandlerFoundException ex) {
        return new ResponseEntity<>("리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }

    // 그 외의 일반적인 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        log.debug(StringUtil.getStackTraceToString(ex));
        return new ResponseEntity<>("서버 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
