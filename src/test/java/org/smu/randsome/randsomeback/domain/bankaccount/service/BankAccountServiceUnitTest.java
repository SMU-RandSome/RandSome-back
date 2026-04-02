package org.smu.randsome.randsomeback.domain.bankaccount.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.bankaccount.entity.BankAccount;
import org.smu.randsome.randsomeback.domain.bankaccount.implement.BankAccountReader;
import org.smu.randsome.randsomeback.fixture.BankAccountFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class BankAccountServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    BankAccountService bankAccountService;

    @Mock
    BankAccountReader bankAccountReader;

    @Test
    void 계좌_조회에_성공하면_BankAccount를_반환한다() {
        // given
        BankAccount bankAccount = BankAccountFixture.create();
        given(bankAccountReader.findByMemberId(1L)).willReturn(bankAccount);

        // when
        BankAccount result = bankAccountService.findByMemberId(1L);

        // then
        assertThat(result).isEqualTo(bankAccount);
    }

    @Test
    void 계좌가_존재하지_않으면_예외가_발생한다() {
        // given
        willThrow(new CoreException(ErrorType.NOT_FOUND_BANK_ACCOUNT))
                .given(bankAccountReader).findByMemberId(any());

        // when & then
        assertThatThrownBy(() -> bankAccountService.findByMemberId(999L))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_BANK_ACCOUNT.getMessage());
    }

}