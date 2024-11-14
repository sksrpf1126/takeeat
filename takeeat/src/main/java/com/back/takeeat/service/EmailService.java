package com.back.takeeat.service;

import com.back.takeeat.common.exception.BaseException;
import com.back.takeeat.common.exception.ErrorCode;
import com.back.takeeat.domain.user.EmailAuth;
import com.back.takeeat.repository.EmailAuthRepository;
import com.back.takeeat.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final MemberRepository memberRepository;
    private final EmailAuthRepository emailAuthRepository;
    private final RedisService redisService;
    private final AsyncService asyncService;

    @Transactional
    public void authenticationEmail(String email) {

        if(memberRepository.existsByEmail(email)) {
            throw new BaseException(ErrorCode.MEMBER_EXISTS);
        }

        String authCode = this.createCode();

        emailAuthRepository.save(EmailAuth.builder()
                .email(email)
                .authCode(authCode)
                .build());

        asyncService.sendEmail(email, authCode, "[TakeEat] 회원가입 이메일 인증");
    }

    @Transactional
    public void authenticationEmailWithRedis(String email) {

        if(memberRepository.existsByEmail(email)) {
            throw new BaseException(ErrorCode.MEMBER_EXISTS);
        }

        String authCode = this.createCode();

        redisService.setAuthCode(email, authCode);

        asyncService.sendEmail(email, authCode, "[TakeEat] 회원가입 이메일 인증");
    }

    @Transactional
    public void findMemberLoginIdSendEmail(String email) {

        if(!memberRepository.existsByEmail(email)) {
            throw new BaseException(ErrorCode.MEMBER_NOT_FOUND);
        }

        String authCode = this.createCode();

        emailAuthRepository.save(EmailAuth.builder()
                .email(email)
                .authCode(authCode)
                .build());

        asyncService.sendEmail(email, authCode, "[TakeEat] 아이디 찾기 인증");
    }

    @Transactional(readOnly = true)
    public void validateAuthCode(String email, String authCode, BindingResult bindingResult) {
        String redisAuthCode = redisService.getAuthCode(email);

        if(!StringUtils.hasText(redisAuthCode)) {
            bindingResult.rejectValue("authCode", "required", "해당 이메일로 인증코드가 발송되지 않았거나 만료되었습니다.");
        }else if(!redisAuthCode.equals(authCode)) {
            bindingResult.rejectValue("authCode", "required", "인증코드를 다시 확인해 주세요.");
        }

    }

    private String createCode() {
        int length = 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            log.debug("MemberService.createCode() exception occur");
            throw new BaseException(ErrorCode.NO_SUCH_AUTH_CODE);
        }
    }

}
