package com.bready.server.global.auth;

import com.bready.server.auth.exception.AuthErrorCase;
import com.bready.server.global.exception.ApplicationException;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApplicationException(AuthErrorCase.INVALID_TOKEN);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Long) {
            return principal;
        }

        try {
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException e) {
            throw new ApplicationException(AuthErrorCase.INVALID_TOKEN);
        }
    }
}
