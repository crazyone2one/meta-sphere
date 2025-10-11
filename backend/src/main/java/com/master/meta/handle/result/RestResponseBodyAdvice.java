package com.master.meta.handle.result;

import com.master.meta.handle.annotation.NoResultHolder;
import com.master.meta.utils.JSON;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@RestControllerAdvice
public class RestResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) || StringHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter parameter,
                                  @NonNull MediaType mediaType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> converterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        // 处理空值
        if (body == null && StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return JSON.toJSONString(ResultHolder.success(null));
        }
        // 直接返回swagger请求结果
        if ("/v3/api-docs/swagger-config".equals(request.getURI().getPath())) {
            return body;
        }
        if (parameter.hasMethodAnnotation(NoResultHolder.class)) {
            return body;
        }
        if (!(body instanceof ResultHolder)) {
            if (body instanceof String) {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return JSON.toJSONString(ResultHolder.success(body));
            }
            return ResultHolder.success(body);
        }
        return body;
    }
}
