SET FOREIGN_KEY_CHECKS = 0;

/* 숫자 테이블 (0 ~ 99,999) */

DROP TABLE IF EXISTS numbers;

CREATE TABLE numbers AS
SELECT
    a.n
        + b.n * 10
        + c.n * 100
        + d.n * 1000
        + e.n * 10000 AS num
FROM
    (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a,
    (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b,
    (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) c,
    (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d,
    (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) e;

/* Plan 1,000개 생성 */
INSERT INTO plans (owner_id, title, plan_date, region, status, created_at, updated_at)
SELECT
    1,
    CONCAT('plan-', num),
    CURDATE() - INTERVAL (num % 30) DAY,
    'SEOUL',
    'ACTIVE',
    NOW(),
    NOW()
FROM numbers
    LIMIT 1000;

/* Trigger 500,000개 생성
   - plan_id 랜덤 매핑
   - trigger_type 랜덤 분포 */
INSERT INTO triggers (
    plan_id,
    category_id,
    candidate_id,
    trigger_type,
    occurred_at,
    created_at,
    updated_at
)
SELECT
    FLOOR(1 + RAND() * 1000),      -- plan_id
    1,                             -- category_id
    1,                             -- candidate_id
    ELT(
            FLOOR(1 + RAND() * 5),
            'WEATHER_BAD',
            'WAITING_TOO_LONG',
            'PLACE_CLOSED',
            'FATIGUE',
            'DISTANCE_TOO_FAR'
    ),
    NOW() - INTERVAL FLOOR(RAND() * 30) DAY,
    NOW(),
    NOW()
FROM numbers n1
    JOIN numbers n2
    LIMIT 500000;

/* Decision 생성 (Trigger 1:1) - SWITCH 약 30%, KEEP 약 70% */
INSERT INTO decisions (
    trigger_id,
    decision_type,
    decided_at,
    created_at,
    updated_at
)
SELECT
    t.id,
    IF(RAND() < 0.3, 'SWITCH', 'KEEP'),
    NOW(),
    NOW(),
    NOW()
FROM triggers t;

/* SwitchLog 생성 (SWITCH만) */
INSERT INTO switch_logs (
    decision_id,
    from_candidate_id,
    to_candidate_id,
    created_at,
    updated_at
)
SELECT
    d.id,
    1,
    2,
    NOW(),
    NOW()
FROM decisions d
WHERE d.decision_type = 'SWITCH';

SET FOREIGN_KEY_CHECKS = 1;
