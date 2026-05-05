package org.smu.randsome.randsomeback.domain.terms.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.terms.entity.Terms;
import org.smu.randsome.randsomeback.domain.terms.entity.TermsAgreement;
import org.smu.randsome.randsomeback.domain.terms.repository.TermsAgreementJpaRepository;
import org.smu.randsome.randsomeback.domain.terms.repository.TermsJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

class TermsAgreementManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    TermsAgreementManager termsAgreementManager;

    @Mock
    TermsJpaRepository termsJpaRepository;

    @Mock
    TermsAgreementJpaRepository termsAgreementJpaRepository;

    @Test
    void 활성_약관_전체에_대한_동의_내역을_저장한다() {
        // given
        var memberId = 1L;
        var terms1 = mock(Terms.class);
        var terms2 = mock(Terms.class);
        given(terms1.getId()).willReturn(10L);
        given(terms2.getId()).willReturn(20L);
        given(termsJpaRepository.findAllByStatus(EntityStatus.ACTIVE))
                .willReturn(List.of(terms1, terms2));
        given(termsAgreementJpaRepository.saveAll(any())).willAnswer(i -> i.getArgument(0));

        // when
        termsAgreementManager.saveAll(memberId);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TermsAgreement>> captor = ArgumentCaptor.forClass(List.class);
        verify(termsAgreementJpaRepository).saveAll(captor.capture());

        List<TermsAgreement> saved = captor.getValue();
        assertThat(saved).hasSize(2)
                .allMatch(a -> a.getMemberId().equals(memberId))
                .anyMatch(a -> a.getTermsId().equals(10L))
                .anyMatch(a -> a.getTermsId().equals(20L));
    }

    @Test
    void 활성_약관이_없으면_빈_목록을_저장한다() {
        // given
        given(termsJpaRepository.findAllByStatus(EntityStatus.ACTIVE)).willReturn(List.of());
        given(termsAgreementJpaRepository.saveAll(any())).willAnswer(i -> i.getArgument(0));

        // when
        termsAgreementManager.saveAll(1L);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TermsAgreement>> captor = ArgumentCaptor.forClass(List.class);
        verify(termsAgreementJpaRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).isEmpty();
    }

}