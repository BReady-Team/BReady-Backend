package com.bready.server.stats.domain;

import com.bready.server.trigger.domain.TriggerType;

public interface TriggerTypeCount {
    TriggerType getTriggerType();
    long getCount();
}
