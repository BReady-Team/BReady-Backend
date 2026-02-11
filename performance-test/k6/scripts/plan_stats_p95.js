import http from "k6/http";
import { check, sleep } from "k6";


const BASE_URL = __ENV.BASE_URL || "http://host.docker.internal:8080";

export const options = {

    scenarios: {
        stats_load_test: {
            executor: "ramping-vus",

            stages: [
                { duration: "20s", target: 20 }, // 워밍업
                { duration: "40s", target: 50 }, // 중간 부하
                { duration: "40s", target: 80 }, // 실제 압박
                { duration: "20s", target: 0 },
            ],

            gracefulRampDown: "10s",
        },
    },

    thresholds: {

        // 실패율 1% 이하
        http_req_failed: ["rate<0.01"],

        // p95가 600ms 넘으면 FAIL
        http_req_duration: ["p(95)<600"],

    },
};

export default function () {
    const params = {
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${__ENV.ACCESS_TOKEN}`,
        },
        timeout: "10s",
    };

    const joinRes = http.get(
        `${BASE_URL}/api/v1/stats/plans?period=WEEK&limit=10`,
        params
    );

    check(joinRes, {
        "JOIN status 200": (r) => r.status === 200,
    });

    if (joinRes.status !== 200) {
        console.log(`JOIN FAILED: ${joinRes.status}`);
    }

    const matRes = http.get(
        `${BASE_URL}/api/v1/stats/plans/materialized?period=WEEK&limit=10`,
        params
    );

    check(matRes, {
        "MAT status 200": (r) => r.status === 200,
    });

    sleep(1);
}
