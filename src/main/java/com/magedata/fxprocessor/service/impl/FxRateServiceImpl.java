package com.magedata.fxprocessor.service.impl;

import com.magedata.fxprocessor.config.CacheConfig;
import com.magedata.fxprocessor.entity.ExchangeRateSnapshotEntity;
import com.magedata.fxprocessor.exception.FxRateUnavailableException;
import com.magedata.fxprocessor.exception.InvalidCurrencyException;
import com.magedata.fxprocessor.repository.ExchangeRateSnapshotRepository;
import com.magedata.fxprocessor.service.FrankfurterResponse;
import com.magedata.fxprocessor.service.FxRateResult;
import com.magedata.fxprocessor.service.FxRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FxRateServiceImpl implements FxRateService {

    private static final Logger log = LoggerFactory.getLogger(FxRateServiceImpl.class);

    private final RestClient restClient;
    private final ExchangeRateSnapshotRepository snapshotRepository;
    private final String baseUrl;

    @Lazy
    @Autowired
    private FxRateServiceImpl self;

    private static final Map<String, BigDecimal> BASELINE_RATES = new ConcurrentHashMap<>();

    static {
        BASELINE_RATES.put("EUR_USD", new BigDecimal("1.085000"));
        BASELINE_RATES.put("USD_EUR", new BigDecimal("0.921600"));
        BASELINE_RATES.put("GBP_USD", new BigDecimal("1.295000"));
        BASELINE_RATES.put("USD_GBP", new BigDecimal("0.772200"));
        BASELINE_RATES.put("JPY_USD", new BigDecimal("0.006700"));
        BASELINE_RATES.put("USD_JPY", new BigDecimal("149.250000"));
        BASELINE_RATES.put("CAD_USD", new BigDecimal("0.735000"));
        BASELINE_RATES.put("AUD_USD", new BigDecimal("0.655000"));
        BASELINE_RATES.put("CHF_USD", new BigDecimal("1.135000"));

        BASELINE_RATES.put("USD_AED", new BigDecimal("3.672500"));
        BASELINE_RATES.put("AED_USD", new BigDecimal("0.272294"));
        BASELINE_RATES.put("EUR_AED", new BigDecimal("3.984663"));
        BASELINE_RATES.put("AED_EUR", new BigDecimal("0.250962"));
        BASELINE_RATES.put("GBP_AED", new BigDecimal("4.755888"));
        BASELINE_RATES.put("AED_GBP", new BigDecimal("0.210266"));
        BASELINE_RATES.put("USD_SAR", new BigDecimal("3.750000"));
        BASELINE_RATES.put("SAR_USD", new BigDecimal("0.266667"));

        BASELINE_RATES.put("USD_INR", new BigDecimal("83.950000"));
        BASELINE_RATES.put("INR_USD", new BigDecimal("0.011912"));
        BASELINE_RATES.put("USD_SGD", new BigDecimal("1.345000"));
        BASELINE_RATES.put("SGD_USD", new BigDecimal("0.743494"));
    }

    public FxRateServiceImpl(
            RestClient restClient,
            ExchangeRateSnapshotRepository snapshotRepository,
            @Value("${fx.api.frankfurter.base-url:https://api.frankfurter.dev/v2}") String baseUrl) {
        this.restClient = restClient;
        this.snapshotRepository = snapshotRepository;
        this.baseUrl = baseUrl;
    }

    @Override
    public FxRateResult getExchangeRate(String sourceCurrency, String targetCurrency) {
        String source = sourceCurrency.trim().toUpperCase();
        String target = targetCurrency.trim().toUpperCase();

        if (source.equals(target)) {
            return new FxRateResult(BigDecimal.ONE.setScale(6, RoundingMode.HALF_UP), FxRateResult.SOURCE_IDENTITY);
        }

        boolean isNotFound = false;
        try {
            BigDecimal rate = self.fetchFromLiveApiWithCache(source, target);
            return new FxRateResult(rate, FxRateResult.SOURCE_LIVE_API);
        } catch (HttpClientErrorException ex) {
            isNotFound = true;
            log.warn("Frankfurter v2 rejected currency pair {}/{}: {}", source, target, ex.getMessage());
        } catch (Exception ex) {
            log.warn("Live FX rate fetch failed for {}/{}: {}. Falling back to snapshot.", source, target, ex.getMessage());
        }

        // Check fallback snapshot in database
        Optional<ExchangeRateSnapshotEntity> snapshot =
                snapshotRepository.findBySourceCurrencyIgnoreCaseAndTargetCurrencyIgnoreCase(source, target);

        if (snapshot.isPresent()) {
            BigDecimal snapshotRate = snapshot.get().getRate().setScale(6, RoundingMode.HALF_UP);
            log.info("Using DB fallback rate for {}/{}: {}", source, target, snapshotRate);
            return new FxRateResult(snapshotRate, FxRateResult.SOURCE_FALLBACK_SNAPSHOT);
        }

        // Check baseline rates
        String pairKey = source + "_" + target;
        if (BASELINE_RATES.containsKey(pairKey)) {
            BigDecimal baselineRate = BASELINE_RATES.get(pairKey).setScale(6, RoundingMode.HALF_UP);
            log.warn("Using baseline fallback rate for {}/{}: {}", source, target, baselineRate);
            return new FxRateResult(baselineRate, FxRateResult.SOURCE_FALLBACK_EMERGENCY);
        }

        String reversePairKey = target + "_" + source;
        if (BASELINE_RATES.containsKey(reversePairKey)) {
            BigDecimal invertedRate = BigDecimal.ONE.divide(BASELINE_RATES.get(reversePairKey), 6, RoundingMode.HALF_UP);
            log.warn("Using inverted baseline fallback rate for {}/{}: {}", source, target, invertedRate);
            return new FxRateResult(invertedRate, FxRateResult.SOURCE_FALLBACK_EMERGENCY);
        }

        if (isNotFound) {
            throw new InvalidCurrencyException(
                    "Unsupported currency for FX conversion: " + source + " to " + target);
        }

        throw new FxRateUnavailableException(
                "FX rate provider unreachable and no cached snapshot available for " + source + " to " + target);
    }

    @Cacheable(value = CacheConfig.FX_RATES_CACHE, key = "#source + '_' + #target")
    public BigDecimal fetchFromLiveApiWithCache(String source, String target) {
        String url = String.format("%s/rate/%s/%s", baseUrl, source, target);

        FrankfurterResponse response = restClient.get()
                .uri(url)
                .retrieve()
                .body(FrankfurterResponse.class);

        if (response == null || response.getRate() == null) {
            throw new FxRateUnavailableException("No rate returned from Frankfurter v2 for " + source + "/" + target);
        }

        BigDecimal rate = response.getRate().setScale(6, RoundingMode.HALF_UP);
        persistSnapshotSafely(source, target, rate);

        return rate;
    }

    @Transactional
    public void persistSnapshotSafely(String source, String target, BigDecimal rate) {
        try {
            Optional<ExchangeRateSnapshotEntity> existing =
                    snapshotRepository.findBySourceCurrencyIgnoreCaseAndTargetCurrencyIgnoreCase(source, target);

            ExchangeRateSnapshotEntity entity = existing.orElseGet(() -> ExchangeRateSnapshotEntity.builder()
                    .sourceCurrency(source)
                    .targetCurrency(target)
                    .build());

            entity.setRate(rate);
            entity.setUpdatedAt(OffsetDateTime.now());
            snapshotRepository.save(entity);
        } catch (Exception e) {
            log.error("Could not persist rate snapshot for {}/{}: {}", source, target, e.getMessage());
        }
    }
}
