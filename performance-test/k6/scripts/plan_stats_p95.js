import http from "k6/http";
import { check, sleep } from "k6";


const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

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
        },
        timeout: "10s",
    };

    const joinRes = http.get(
        `${BASE_URL}/api/v1/plans/stats?period=WEEK&limit=10&mode=join`,
        params
    );

    check(joinRes, {
        "JOIN status 200": (r) => r.status === 200,
    });


    const matRes = http.get(
        `${BASE_URL}/api/v1/plans/stats?period=WEEK&limit=10&mode=mat`,
        params
    );

    check(matRes, {
        "MAT status 200": (r) => r.status === 200,
    });

    sleep(1);
}
