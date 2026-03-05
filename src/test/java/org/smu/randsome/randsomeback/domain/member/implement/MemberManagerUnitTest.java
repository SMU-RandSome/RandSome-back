package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.vo.Password;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    MemberManager memberManager;

    @Mock
    MemberJpaRepository memberJpaRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    void 회원을_생성한다() {
        // given
        given(memberJpaRepository.existsByEmail_AddressAndStatus(any(String.class), any(EntityStatus.class)))
                .willReturn(false);
        String encodedPassword = "encoded-password";
        given(passwordEncoder.encode(MemberFixture.DEFAULT_RAW_PASSWORD)).willReturn(encodedPassword);
        // save()에 넘긴 인자를 그대로 반환하도록 설정
        given(memberJpaRepository.save(any(Member.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        Member member = memberManager.create(
                MemberFixture.createCredentials(),
                MemberFixture.createBasicInfo(),
                MemberFixture.createMemberSocialProfile());

        // then
        assertThat(member).isNotNull().extracting(
                Member::getEmail,
                Member::getLegalName,
                Member::getRole,
                Member::getSocialProfile,
                Member::getMbti,
                Member::getGender,
                Member::getPassword
        ).containsExactly(
                MemberFixture.email(),
                MemberFixture.DEFAULT_LEGAL_NAME,
                Role.ROLE_MEMBER,
                MemberFixture.socialProfile(),
                MemberFixture.DEFAULT_MBTI,
                MemberFixture.DEFAULT_GENDER,
                new Password(encodedPassword)
        );
    }

    @Test
    void 이미_존재하는_이메일이면_예외가_발생한다() {
        // given
        given(memberJpaRepository.existsByEmail_AddressAndStatus(any(String.class), any(EntityStatus.class)))
                .willReturn(true);

        // when // then
        assertThatThrownBy(() -> memberManager.create(
                MemberFixture.createCredentials(),
                MemberFixture.createBasicInfo(),
                MemberFixture.createMemberSocialProfile()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.DUPLICATE_EMAIL.getMessage());
    }

}