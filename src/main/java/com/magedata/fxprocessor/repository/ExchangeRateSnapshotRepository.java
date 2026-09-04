package com.magedata.fxprocessor.repository;

import com.magedata.fxprocessor.entity.ExchangeRateSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateSnapshotRepository extends JpaRepository<ExchangeRateSnapshotEntity, Long> {

    Optional<ExchangeRateSnapshotEntity> findBySourceCurrencyIgnoreCaseAndTargetCurrencyIgnoreCase(
            String sourceCurrency, String targetCurrency);
}
