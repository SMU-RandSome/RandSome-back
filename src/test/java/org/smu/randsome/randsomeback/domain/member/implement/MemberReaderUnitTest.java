package org.smu.randsome.randsomeback.domain.member.implement;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberReaderUnitTest extends UnitTestSupport {

    MemberReader memberReader;

    @Mock
    MemberRepository memberRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        memberReader = new MemberReader(memberRepository, passwordEncoder);
    }

    @Test
    void 정지된_회원이_올바른_비밀번호로_로그인_시_SUSPENDED_MEMBER_예외가_발생한다() {
        // given
        Member member = MemberFixture.create();
        member.suspend();

        given(memberRepository.findByEmail_AddressAndStatusNot(MemberFixture.DEFAULT_EMAIL, EntityStatus.DELETED))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches(eq(MemberFixture.DEFAULT_RAW_PASSWORD), any()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberReader.findByAccount(MemberFixture.DEFAULT_EMAIL, MemberFixture.DEFAULT_RAW_PASSWORD))
                .isInstanceOf(CoreException.class)
                .extracting(e -> ((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.SUSPENDED_MEMBER);
    }

    @Test
    void 정지된_회원이_잘못된_비밀번호로_로그인_시_INVALID_ACCOUNT_예외가_발생한다() {
        // given
        Member member = MemberFixture.create();
        member.suspend();

        given(memberRepository.findByEmail_AddressAndStatusNot(MemberFixture.DEFAULT_EMAIL, EntityStatus.DELETED))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches(eq("wrongPassword"), any()))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberReader.findByAccount(MemberFixture.DEFAULT_EMAIL, "wrongPassword"))
                .isInstanceOf(CoreException.class)
                .extracting(e -> ((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.INVALID_ACCOUNT);
    }

    @Test
    void 자율전공이면_학과_제외_조건_없이_후보_ID를_조회한다() {
        // given
        given(memberRepository.findCandidateIdsByGender(
                Gender.FEMALE, Role.ROLE_CANDIDATE, EntityStatus.ACTIVE))
                .willReturn(List.of());

        // when
        memberReader.findCandidatesByGender(Gender.FEMALE, Department.SELF_DIRECTED_MAJOR, 10);

        // then
        verify(memberRepository).findCandidateIdsByGender(
                Gender.FEMALE, Role.ROLE_CANDIDATE, EntityStatus.ACTIVE);
        verify(memberRepository, never()).findCandidateIdsByGenderExcludingDepartment(
                any(), any(), any(), any());
    }

    @Test
    void 자율전공이_아니면_같은_학과를_제외하고_후보_ID를_조회한다() {
        // given
        given(memberRepository.findCandidateIdsByGenderExcludingDepartment(
                Gender.FEMALE, Department.SOFTWARE, Role.ROLE_CANDIDATE, EntityStatus.ACTIVE))
                .willReturn(List.of());

        // when
        memberReader.findCandidatesByGender(Gender.FEMALE, Department.SOFTWARE, 10);

        // then
        verify(memberRepository).findCandidateIdsByGenderExcludingDepartment(
                Gender.FEMALE, Department.SOFTWARE, Role.ROLE_CANDIDATE, EntityStatus.ACTIVE);
        verify(memberRepository, never()).findCandidateIdsByGender(any(), any(), any());
    }

}
