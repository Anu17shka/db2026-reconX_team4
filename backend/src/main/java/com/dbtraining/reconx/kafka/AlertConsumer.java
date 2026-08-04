package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.alert.AlertSink;
import com.dbtraining.reconx.dto.SystemAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(AlertConsumer.class);

    private final AlertSink sink;

    public AlertConsumer(AlertSink sink) {
        this.sink = sink;
    }

    @KafkaListener(
            topics = "system-alerts",
            groupId = "alert-service"
    )
    public void onAlert(SystemAlert alert) {

        log.warn(
                "ALERT severity={} code={} message={}",
                alert.severity(),
                alert.code(),
                alert.message()
        );

        sink.notify(alert);
    }
}