package com.woo.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.woo.exception.config.ErrorConfig;
import com.woo.exception.dto.ErrorInfo;
import com.woo.exception.util.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler implements HandlerExceptionResolver {

    private ErrorConfig errorConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final static List<Class<? extends Exception>> BUSINESS_EXCEPTION = List.of(BizException.class);
    private final static List<Class<? extends Exception>> BAD_REQUEST = List.of(MethodArgumentNotValidException.class, HttpMessageNotReadableException.class, TypeMismatchException.class, MissingServletRequestParameterException.class, BindException.class);
    private final static List<Class<? extends Exception>> UNAUTHORIZED = List.of(AuthenticationException.class, AccessDeniedException.class);
    private final static List<Class<? extends Exception>> NOT_FOUND = List.of(NoHandlerFoundException.class, NoResourceFoundException.class);
    private final static List<Class<? extends Exception>> METHOD_NOT_ALLOWED = List.of(HttpRequestMethodNotSupportedException.class);
    private final static List<Class<? extends Exception>> UNSUPPORTED_MEDIA_TYPE = List.of(HttpMediaTypeNotSupportedException.class);
    private final static List<Class<? extends Exception>> INTERNAL_SERVER_ERROR = List.of(Exception.class);

    public static RestExceptionHandler setErrorConfig(ErrorConfig errorConfig) {
        return new RestExceptionHandler(errorConfig);
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ErrorInfo errorInfo = null;

        if (isRequestMatch(ex, BUSINESS_EXCEPTION))             errorInfo = setBizErrorInfo((BizException) ex);
        else if (isRequestMatch(ex, BAD_REQUEST))               errorInfo = ErrorInfo.builder().status(400).message("잘못된 요청입니다. 요청 파라미터를 확인해주세요.").build();
        else if (isRequestMatch(ex, UNAUTHORIZED))              errorInfo = ErrorInfo.builder().status(401).message("접근 권한이 없습니다. 관리자에게 문의하세요.").build();
        else if (isRequestMatch(ex, NOT_FOUND))                 errorInfo = ErrorInfo.builder().status(404).message("요청하신 리소스를 찾을 수 없습니다. URL을 확인해주세요.").build();
        else if (isRequestMatch(ex, METHOD_NOT_ALLOWED))        errorInfo = ErrorInfo.builder().status(405).message("허용되지 않은 메소드입니다. 요청 방식을 확인해주세요.").build();
        else if (isRequestMatch(ex, UNSUPPORTED_MEDIA_TYPE))    errorInfo = ErrorInfo.builder().status(415).message("지원하지 않는 미디어 타입입니다. 요청 데이터를 확인해주세요.").build();
        else if (isRequestMatch(ex, INTERNAL_SERVER_ERROR))     errorInfo = ErrorInfo.builder().status(500).message("서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.").build();

        setErrorInfoToResponse(response, ex, errorInfo);

        return new ModelAndView();
    }

    private boolean isRequestMatch(Exception ex, List<Class<? extends Exception>> exceptionList) {
        return exceptionList.stream().anyMatch(clazz -> clazz.isInstance(ex));
    }

    private ErrorInfo setBizErrorInfo(BizException ex) {
        ErrorInfo errorInfo = null;

        errorInfo = errorConfig.getErrors().get(ex.getKey());
        if (errorInfo == null) errorInfo = ErrorInfo.builder().status(500).message("알 수 없는 에러입니다.").build();

        return errorInfo;
    }

    private void setErrorInfoToResponse(HttpServletResponse response, Exception ex, ErrorInfo errorInfo) {
        log.error(ex.getClass().getSimpleName() + " : " + errorInfo.getMessage());
        ex.printStackTrace();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorInfo.getStatus());
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(objectMapper.writeValueAsString(errorInfo));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
