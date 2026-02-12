USE bready;


--plan_stats 비우기
DELETE FROM plan_stats;


-- WEEK / MONTH / ALL 통계 재빌드
-- triggers.occurred_at 기준으로 기간 필터
-- switches는 switch_logs.created_at 기준으로 기간 필터

/* WEEK (최근 7일) */
INSERT INTO plan_stats (
    plan_id,
    period,
    total_triggers,
    total_switches,
    reliability_score,
    created_at,
    updated_at
)
SELECT
    p.id AS plan_id,
    'WEEK' AS period,

    /* total_triggers */
    (
        SELECT COUNT(*)
        FROM triggers t
        WHERE t.plan_id = p.id
          AND t.occurred_at >= NOW() - INTERVAL 7 DAY
    ) AS total_triggers,

    /* total_switches */
    (
        SELECT COUNT(*)
        FROM switch_logs sl
        JOIN decisions d ON d.id = sl.decision_id
        JOIN triggers  t ON t.id = d.trigger_id
        WHERE t.plan_id = p.id
        AND sl.created_at >= NOW() - INTERVAL 7 DAY
    ) AS total_switches,


    ROUND(
        (1 - (
            (
                SELECT COUNT(*)
                FROM switch_logs sl
                JOIN decisions d ON d.id = sl.decision_id
                JOIN triggers  t ON t.id = d.trigger_id
                WHERE t.plan_id = p.id
                  AND sl.created_at >= NOW() - INTERVAL 7 DAY
    ) / GREATEST(
        (
            SELECT COUNT(*)
            FROM triggers t
            WHERE t.plan_id = p.id
              AND t.occurred_at >= NOW() - INTERVAL 7 DAY
        ), 1
       )
    )) * 100
    , 2) AS reliability_score,

    NOW(),
    NOW()
FROM plans p;


/* MONTH (최근 1개월) */
INSERT INTO plan_stats (
    plan_id,
    period,
    total_triggers,
    total_switches,
    reliability_score,
    created_at,
    updated_at
)
SELECT
    p.id AS plan_id,
    'MONTH' AS period,

    (
        SELECT COUNT(*)
        FROM triggers t
        WHERE t.plan_id = p.id
          AND t.occurred_at >= NOW() - INTERVAL 1 MONTH
        ) AS total_triggers,

    (
        SELECT COUNT(*)
        FROM switch_logs sl
        JOIN decisions d ON d.id = sl.decision_id
        JOIN triggers  t ON t.id = d.trigger_id
        WHERE t.plan_id = p.id
          AND sl.created_at >= NOW() - INTERVAL 1 MONTH
    ) AS total_switches,

    ROUND(
        (1 - (
            (
                SELECT COUNT(*)
                FROM switch_logs sl
                JOIN decisions d ON d.id = sl.decision_id
                JOIN triggers  t ON t.id = d.trigger_id
                WHERE t.plan_id = p.id
                  AND sl.created_at >= NOW() - INTERVAL 1 MONTH
            ) / GREATEST(
                (
                    SELECT COUNT(*)
                    FROM triggers t
                    WHERE t.plan_id = p.id
                      AND t.occurred_at >= NOW() - INTERVAL 1 MONTH
                ), 1
            )
        )) * 100
    , 2) AS reliability_score,

    NOW(),
    NOW()
FROM plans p;


/* ALL (전체) -> JOIN으로 한 번에 계산 (기간 없음) */
INSERT INTO plan_stats (
    plan_id,
    period,
    total_triggers,
    total_switches,
    reliability_score,
    created_at,
    updated_at
)
SELECT
    p.id AS plan_id,
    'ALL' AS period,
    COUNT(DISTINCT t.id) AS total_triggers,
    COUNT(DISTINCT sl.id) AS total_switches,
    ROUND((1 - (COUNT(DISTINCT sl.id) / GREATEST(COUNT(DISTINCT t.id), 1))) * 100, 2) AS reliability_score,
    NOW(),
    NOW()
FROM plans p
         LEFT JOIN triggers t ON t.plan_id = p.id
         LEFT JOIN decisions d ON d.trigger_id = t.id
         LEFT JOIN switch_logs sl ON sl.decision_id = d.id
GROUP BY p.id;

-- 옵티마이저 통계 갱신
ANALYZE TABLE plan_stats;

-- 확인
SELECT period, COUNT(*) AS rows_count FROM plan_stats GROUP BY period;
SELECT * FROM plan_stats LIMIT 5;
