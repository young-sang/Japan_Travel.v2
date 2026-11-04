import { useEffect } from 'react';

const SITE = '일본 구석구석';

//  탭 제목을 "페이지 | 일본 구석구석" 으로 둔다. title 이 비면(상세가 아직 로딩 중 등) 사이트 이름만 쓴다 (D-068)
export default function usePageTitle(title) {
  useEffect(() => {
    document.title = title ? `${title} | ${SITE}` : SITE;
  }, [title]);
}
