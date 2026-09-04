package com.magedata.fxprocessor.service;

import com.magedata.fxprocessor.entity.ExchangeRateSnapshotEntity;
import com.magedata.fxprocessor.exception.FxRateUnavailableException;
import com.magedata.fxprocessor.repository.ExchangeRateSnapshotRepository;
import com.magedata.fxprocessor.service.impl.FxRateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FxRateServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private ExchangeRateSnapshotRepository snapshotRepository;

    private FxRateServiceImpl fxRateService;

    @BeforeEach
    void setUp() {
        fxRateService = new FxRateServiceImpl(
                restClient,
                snapshotRepository,
                "https://api.frankfurter.dev/v2"
        );
        ReflectionTestUtils.setField(fxRateService, "self", fxRateService);
    }

    @Test
    @DisplayName("Identity conversion when source and target currencies match")
    void testIdentityConversion() {
        FxRateResult result = fxRateService.getExchangeRate("USD", "USD");

        assertNotNull(result);
        assertEquals(new BigDecimal("1.000000"), result.rate());
        assertEquals(FxRateResult.SOURCE_IDENTITY, result.rateSource());
        verifyNoInteractions(snapshotRepository);
    }

    @Test
    @DisplayName("Fallback to DB snapshot when live API throws exception")
    void testFallbackToDbSnapshotOnApiFailure() {
        ExchangeRateSnapshotEntity snapshot = ExchangeRateSnapshotEntity.builder()
                .id(1L)
                .sourceCurrency("EUR")
                .targetCurrency("USD")
                .rate(new BigDecimal("1.085000"))
                .updatedAt(OffsetDateTime.now())
                .build();

        when(snapshotRepository.findBySourceCurrencyIgnoreCaseAndTargetCurrencyIgnoreCase("EUR", "USD"))
                .thenReturn(Optional.of(snapshot));

        FxRateResult result = fxRateService.getExchangeRate("EUR", "USD");

        assertNotNull(result);
        assertEquals(new BigDecimal("1.085000"), result.rate());
        assertEquals(FxRateResult.SOURCE_FALLBACK_SNAPSHOT, result.rateSource());
    }

    @Test
    @DisplayName("Fallback to Emergency static baseline when live API and DB snapshot both miss")
    void testFallbackToEmergencyBaseline() {
        when(snapshotRepository.findBySourceCurrencyIgnoreCaseAndTargetCurrencyIgnoreCase("GBP", "USD"))
                .thenReturn(Optional.empty());

        FxRateResult result = fxRateService.getExchangeRate("GBP", "USD");

        assertNotNull(result);
        assertEquals(new BigDecimal("1.295000"), result.rate());
        assertEquals(FxRateResult.SOURCE_FALLBACK_EMERGENCY, result.rateSource());
    }

    @Test
    @DisplayName("Throws FxRateUnavailableException when pair cannot be resolved")
    void testUnresolvablePairThrowsException() {
        when(snapshotRepository.findBySourceCurrencyIgnoreCaseAndTargetCurrencyIgnoreCase(anyString(), anyString()))
                .thenReturn(Optional.empty());

        assertThrows(FxRateUnavailableException.class, () ->
                fxRateService.getExchangeRate("XYZ", "ABC"));
    }
}
