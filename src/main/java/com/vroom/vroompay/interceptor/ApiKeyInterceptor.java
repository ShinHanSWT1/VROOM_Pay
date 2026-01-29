package com.vroom.vroompay.interceptor;

import com.vroom.vroompay.mapper.PartnerMapper;
import com.vroom.vroompay.vo.PartnerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API 키 인증 인터셉터
 * 모든 /pay/** 요청에서 X-API-KEY 헤더를 검증
 */
@Slf4j
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Autowired
    private PartnerMapper partnerMapper;

    /**
     * 컨트롤러 실행 전에 API 키 검증
     * @return true: 통과 (컨트롤러 실행) / false: 차단 (401 응답)
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 요청 헤더에서 API 키 꺼내기
        String apiKey = request.getHeader("X-API-KEY");

        // API 키가 없으면 401 Unauthorized
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API 키가 없습니다.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("API Key is missing");
            return false;  // 여기서 끝! 컨트롤러 실행 안 됨
        }

        // DB에서 API 키 조회 (활성화된 파트너만)
        PartnerVO partner = partnerMapper.findByApiKey(apiKey);

        // 조회 결과 없으면 401 Unauthorized
        if (partner == null) {
            log.warn("유효하지 않은 API 키: {}", apiKey);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API Key");
            return false;  // 여기서 끝! 컨트롤러 실행 안 됨
        }

        // 인증 성공! 컨트롤러로 진행
        log.info("API 인증 성공 - 파트너: {}", partner.getPartnerName());
        return true;
    }
}