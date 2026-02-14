ALTER TABLE plan_stats
    ADD CONSTRAINT uk_plan_stats_plan_period
        UNIQUE (plan_id, period);

CREATE INDEX idx_plan_stats_period_plan
    ON plan_stats(period, plan_id);

CREATE INDEX idx_triggers_plan_occurred
    ON triggers(plan_id, occurred_at);

CREATE INDEX idx_switch_logs_created
    ON switch_logs(created_at);
