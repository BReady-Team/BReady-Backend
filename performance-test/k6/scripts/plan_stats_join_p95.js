import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://host.docker.internal:8080";
const TOKEN = __ENV.ACCESS_TOKEN;

export const options = {
    scenarios: {
        join_load_test: {
            executor: "ramping-vus",
            stages: [
                { duration: "20s", target: 20 },
                { duration: "40s", target: 50 },
                { duration: "40s", target: 80 },
                { duration: "20s", target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<600"],
    },
};

export default function () {
    const res = http.get(
        `${BASE_URL}/api/v1/stats/plans/join?period=WEEK&limit=10`,
        {
            headers: {
                Authorization: `Bearer ${TOKEN}`,
            },
            timeout: "10s",
        }
    );

    check(res, {
        "JOIN 200": (r) => r.status === 200,
    });

    sleep(1);
}
