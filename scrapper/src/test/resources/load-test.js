import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

export const options = {
    stages: [
        { duration: '1m', target: 50 },
        { duration: '5m', target: 50 },
        { duration: '1m', target: 0 },
    ],
};

const BASE_URL = 'http://localhost:8081';

const errors500 = new Counter('errors_500');
const errors502_504 = new Counter('errors_502_504');
const otherErrors = new Counter('errors_other');

function trackErrors(res) {
    if (res.status === 500) {
        errors500.add(1);
    } else if (res.status === 502 || res.status === 504) {
        errors502_504.add(1);
    } else if (res.status >= 400) {
        otherErrors.add(1);
    }
}

export default function () {
    const chatId = Math.floor(Math.random() * 2000) + 1;

    if (Math.random() < 0.95) {
        const res = http.get(${BASE_URL}/links, {
            headers: { 'Tg-Chat-Id': chatId.toString() },
        });
        check(res, {
            'status is 200': (r) => r.status === 200,
        });
        trackErrors(res);
    } else {
        const url = \https://github.com/test/repo-\;
        const addRes = http.post(${BASE_URL}/links, JSON.stringify({ link: url }), {
            headers: { 'Tg-Chat-Id': chatId.toString(), 'Content-Type': 'application/json' },
        });
        trackErrors(addRes);

        if (addRes.status === 200) {
            const delRes = http.del(${BASE_URL}/links, JSON.stringify({ link: url }), {
                headers: { 'Tg-Chat-Id': chatId.toString(), 'Content-Type': 'application/json' },
            });
            trackErrors(delRes);
        }
    }

    sleep(0.1);
}
