package org.smu.randsome.randsomeback.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.jwt.TokenHasher;

@RequiredArgsConstructor
class AuthServiceIntegrationTest extends IntegrationTestSupport {

    final AuthService authService;
    final MemberJpaRepository memberJpaRepository;

    @AfterEach
    void tearDown() {
        memberJpaRepository.deleteAll();
    }

    @Test
    void 로그인_성공_시_refreshToken이_해시화되어_DB에_저장된다() {
        // given
        memberJpaRepository.save(MemberFixture.create());

        // when
        var response = authService.login(MemberFixture.DEFAULT_EMAIL, MemberFixture.DEFAULT_RAW_PASSWORD);

        // then
        var updated = memberJpaRepository.findByEmail_AddressAndStatus(
                MemberFixture.DEFAULT_EMAIL,
                org.smu.randsome.randsomeback.global.entity.EntityStatus.ACTIVE
        ).orElseThrow();

        assertThat(response.refreshToken()).isNotNull();
        assertThat(updated.getRefreshToken()).isEqualTo(TokenHasher.hash(response.refreshToken()));
    }

    @Test
    void 토큰_재발급_성공_시_새_refreshToken이_DB에_반영된다() {
        // given
        memberJpaRepository.save(MemberFixture.create());
        var loginResponse = authService.login(MemberFixture.DEFAULT_EMAIL, MemberFixture.DEFAULT_RAW_PASSWORD);

        // when
        var reissueResponse = authService.reissue(loginResponse.refreshToken());

        // then
        var updated = memberJpaRepository.findByEmail_AddressAndStatus(
                MemberFixture.DEFAULT_EMAIL,
                org.smu.randsome.randsomeback.global.entity.EntityStatus.ACTIVE
        ).orElseThrow();

        assertThat(reissueResponse.accessToken()).isNotNull();
        assertThat(reissueResponse.refreshToken()).isNotNull();
        assertThat(updated.getRefreshToken()).isEqualTo(TokenHasher.hash(reissueResponse.refreshToken()));
    }

}
