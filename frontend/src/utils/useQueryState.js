import { useSearchParams } from 'react-router-dom';

//  목록 화면의 필터 · 정렬 값을 주소(?prefecture=교토부&month=7)에 둔다 (D-084).
//  새로고침하거나 상세에서 뒤로 와도 고른 값이 남는다. 바꿀 때는 replace 라서
//  필터를 누를 때마다 방문 기록이 쌓이지 않는다. 기본값과 같으면 주소에서 뺀다
export default function useQueryState(key, defaultValue = '') {
  const [params, setParams] = useSearchParams();
  const value = params.get(key) ?? defaultValue;

  function setValue(next) {
    setParams((prev) => {
      const p = new URLSearchParams(prev);
      if (next === null || next === undefined || next === '' || next === defaultValue) p.delete(key);
      else p.set(key, String(next));
      return p;
    }, { replace: true });
  }

  return [value, setValue];
}
