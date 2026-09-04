package com.magedata.fxprocessor;

import com.magedata.fxprocessor.repository.ExchangeRateSnapshotRepository;
import com.magedata.fxprocessor.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class FxOrderProcessorApplicationTests {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private ExchangeRateSnapshotRepository snapshotRepository;

    @Test
    void contextLoads() {
    }

}
