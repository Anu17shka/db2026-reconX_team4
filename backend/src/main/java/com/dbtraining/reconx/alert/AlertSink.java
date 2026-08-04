package com.dbtraining.reconx.alert;

import com.dbtraining.reconx.dto.SystemAlert;

public interface AlertSink {
    void notify(SystemAlert alert);
}