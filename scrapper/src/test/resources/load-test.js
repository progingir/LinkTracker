import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '1m', target: 50 },
        { duration: '5m', target: 50 },
        { duration: '1m', target: 0 },
    ],
};

const BASE_URL = 'http://localhost:8081';
const CHAT_ID = 12345;

export default function () {

    if (Math.random() < 0.99) {
        const res = http.get(`${BASE_URL}/links`, {
            headers: { 'Tg-Chat-Id': CHAT_ID.toString() },
        });
        check(res, {
            'status is 200': (r) => r.status === 200,
        });
    } else {
        const url = `https://github.com/test/repo-${Math.floor(Math.random() * 100000)}`;
        const addRes = http.post(`${BASE_URL}/links`, JSON.stringify({ link: url }), {
            headers: { 'Tg-Chat-Id': CHAT_ID.toString(), 'Content-Type': 'application/json' },
        });

        if (addRes.status === 200) {
            http.del(`${BASE_URL}/links`, JSON.stringify({ link: url }), {
                headers: { 'Tg-Chat-Id': CHAT_ID.toString(), 'Content-Type': 'application/json' },
            });
        }
    }

    sleep(0.1);
}
