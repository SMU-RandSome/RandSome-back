package org.smu.randsome.randsomeback.domain.member.implement;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberReaderUnitTest extends UnitTestSupport {

    MemberReader memberReader;

    @Mock
    MemberJpaRepository memberJpaRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        memberReader = new MemberReader(memberJpaRepository, passwordEncoder);
    }

    @Test
    void 자율전공이면_학과_제외_조건_없이_후보를_조회한다() {
        // given
        given(memberJpaRepository.findRandomCandidatesByGender(Gender.FEMALE.name(), 10))
                .willReturn(List.of());

        // when
        memberReader.findCandidatesByGender(Gender.FEMALE, Department.SELF_DIRECTED_MAJOR, 10);

        // then
        verify(memberJpaRepository).findRandomCandidatesByGender(Gender.FEMALE.name(), 10);
        verify(memberJpaRepository, never()).findRandomCandidatesByGenderExcludingDepartment(anyString(), anyString(), anyInt());
    }

    @Test
    void 자율전공이_아니면_같은_학과를_제외하고_후보를_조회한다() {
        // given
        given(memberJpaRepository.findRandomCandidatesByGenderExcludingDepartment(
                Gender.FEMALE.name(), Department.SOFTWARE.name(), 10)).willReturn(List.of());

        // when
        memberReader.findCandidatesByGender(Gender.FEMALE, Department.SOFTWARE, 10);

        // then
        verify(memberJpaRepository).findRandomCandidatesByGenderExcludingDepartment(
                Gender.FEMALE.name(), Department.SOFTWARE.name(), 10);
        verify(memberJpaRepository, never()).findRandomCandidatesByGender(anyString(), anyInt());
    }

}