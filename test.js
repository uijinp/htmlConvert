import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 옵션 설정
export const options = {
  stages: [
    { duration: '1m', target: 10 },   // 1분 동안 가상 사용자를 50명까지 서서히 늘림
    { duration: '3m', target: 10 },   // 50명 상태로 3분간 유지
    { duration: '1m', target: 0 },    // 1분 동안 0명으로 서서히 줄임
  ],
  thresholds: {
    'http_req_failed': ['rate<0.01'], // 에러율은 1% 미만이어야 함
    'http_req_duration': ['p(95)<1000'], // 95%의 요청은 1000ms(1초) 안에 처리되어야 함
  },
};

// 테스트에 사용할 JSON 데이터
const payload = JSON.stringify({
  data: {
    patientNo: "12345",
    department: "내과",
    treatmentPeriod: "2024-01-01 ~ 2024-01-05",
    roomNo: "501",
    insuranceType: "건강보험",
    fee_진찰료: "10000",
    public_진찰료: "9000",
    note_진찰료: "",
    fee_입원료: "50000",
    public_입원료: "45000",
    note_입원료: "",
    fee_투약: "20000",
    public_투약: "18000",
    note_투약: "",
    fee_처치: "30000",
    public_처치: "27000",
    note_처치: "",
    fee_검사: "40000",
    public_검사: "36000",
    note_검사: "",
    fee_재활: "15000",
    public_재활: "13500",
    note_재활: "",
    fee_상급병실료: "100000",
    fee_제증명료: "5000",
    totalCost: "260000",
    totalPublic: "148500",
    totalPersonal: "111500",
    paidAmount: "111500",
    paymentMethod: "카드",
    issueDate: "2024-01-06",
    hospitalName: "제미니 병원"
  }
});

const params = {
  headers: {
    'Content-Type': 'application/json',
  },
};

// 가상 사용자가 실행할 기본 함수
export default function () {
  const res = http.post('http://localhost:8080/api/images/report-templat1/file', payload, params);

  // 응답 검증
  check(res, {
    'is status 200': (r) => r.status === 200,
    'response body is not empty': (r) => r.body.length > 0,
  });

  sleep(1); // 각 요청 사이에 1초 대기
}
