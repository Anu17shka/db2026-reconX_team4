package com.dbtraining.reconx.alert;

import com.dbtraining.reconx.dto.SystemAlert;
import org.springframework.stereotype.Component;

@Component
public class NoopAlertSink implements AlertSink {

    @Override
    public void notify(SystemAlert alert) {
        // Placeholder for Slack/PagerDuty/email integration
    }
}