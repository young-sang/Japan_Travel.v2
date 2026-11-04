//  "2026. 11. 4." — 뉴스 목록 · 상세 · 관리자 표가 같이 쓴다
export default function formatDate(iso) {
  return iso ? new Date(iso).toLocaleDateString('ko-KR') : '';
}
