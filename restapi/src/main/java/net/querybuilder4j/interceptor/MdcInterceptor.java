package net.querybuilder4j.interceptor;

import lombok.extern.slf4j.Slf4j;
import net.querybuilder4j.constants.Constants;
import net.querybuilder4j.utils.ExcludeFromJacocoGeneratedReport;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;


@Slf4j
@ExcludeFromJacocoGeneratedReport
public class MdcInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = UUID.randomUUID().toString();

        /*
        Add a request ID to the response headers so the client knows the ID of their request.  They can then provide this ID
        to us and we can look in the server side logs for their ID in order to troubleshoot an issue they're experiencing.
         */
        response.addHeader(Constants.REQUEST_ID_HEADER, requestId);

        MDC.put("method", request.getMethod());
        MDC.put("remoteUser", request.getRemoteUser());
        MDC.put("uri", request.getRequestURI());
        MDC.put("contentLength", String.valueOf(request.getContentLength()));
        MDC.put("remoteHost", request.getRemoteHost());
        MDC.put("requestId", requestId);
        MDC.put("partyGuid", this.getUserId());
        MDC.put("sessionId", request.getSession().getId());

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        MDC.put("responseCode", String.valueOf(response.getStatus()));
    }

    private String getUserId() {
        return "fake user id...for now";
//        return Optional.ofNullable(SecurityContextHolder.getContext())
//                .map(SecurityContext::getAuthentication)
//                .map(Authentication::getPrincipal)
//                .map(object -> ((Principal) object).getName())
//                .orElse("");
    }

}
