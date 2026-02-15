import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://host.docker.internal:8080";
const TOKEN = __ENV.ACCESS_TOKEN;

export const options = {
    scenarios: {

        // JOIN
        join_test: {
            executor: "ramping-vus",
            exec: "joinScenario",
            stages: [
                { duration: "30s", target: 30 },
                { duration: "1m", target: 80 },
                { duration: "30s", target: 0 },
            ],
        },

        // Materialized
        materialized_test: {
            executor: "ramping-vus",
            exec: "materializedScenario",
            startTime: "10s", // 살짝 늦게 시작 (워밍업 방지)
            stages: [
                { duration: "30s", target: 30 },
                { duration: "1m", target: 80 },
                { duration: "30s", target: 0 },
            ],
        },
    },

    thresholds: {

        "http_req_failed{type:join}": ["rate<0.01"],
        "http_req_failed{type:materialized}": ["rate<0.01"],

        "http_req_duration{type:join}": ["p(95)<2500"],
        "http_req_duration{type:materialized}": ["p(95)<200"],
    },
};

// JOIN
export function joinScenario() {

    const res = http.get(
        `${BASE_URL}/api/v1/stats/plans/join?period=WEEK&limit=10`,
        {
            headers: {
                Authorization: `Bearer ${TOKEN}`,
            },
            tags: { type: "join" },
        }
    );

    check(res, {
        "JOIN 200": (r) => r.status === 200,
    });

    sleep(1);
}

// Materialized
export function materializedScenario() {

    const res = http.get(
        `${BASE_URL}/api/v1/stats/plans/materialized?period=WEEK&limit=10`,
        {
            headers: {
                Authorization: `Bearer ${TOKEN}`,
            },
            tags: { type: "materialized" },
        }
    );

    check(res, {
        "MAT 200": (r) => r.status === 200,
    });

    sleep(1);
}
