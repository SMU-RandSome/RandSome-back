package org.smu.randsome.randsomeback.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberValidator;
import org.smu.randsome.randsomeback.domain.member.service.command.MemberBasicInfo;
import org.smu.randsome.randsomeback.domain.member.service.command.MemberCredentials;
import org.smu.randsome.randsomeback.domain.member.service.command.MemberSocialProfile;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberManager memberManager;
    private final MemberValidator memberValidator;

    public Long create(String emailVerificationToken, MemberCredentials credentials, MemberBasicInfo basicInfo, MemberSocialProfile socialProfile) {
        memberValidator.validateSignUpToken(emailVerificationToken, credentials.email());

        return memberManager.create(credentials, basicInfo, socialProfile).getId();
    }

}