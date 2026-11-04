import { Link } from 'react-router-dom';
import Container from '../components/ui/Container.jsx';
import Button from '../components/ui/Button.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import usePageTitle from '../utils/usePageTitle.js';

//  없는 주소 · 숨긴 화면(/admin) · 삭제한 게시판(/board) 의 옛 링크. 홈으로 몰래 보내지 않고 알려 준다 (D-068)
export default function NotFound() {
  usePageTitle('페이지를 찾을 수 없어요');
  return (
    <Container>
      <EmptyState
        icon="🧭"
        size="lg"
        title="페이지를 찾을 수 없어요"
        description="주소가 바뀌었거나 없어진 페이지예요. 아래에서 다시 찾아보세요."
        primaryAction={<Link to="/"><Button variant="primary">홈으로</Button></Link>}
        secondaryAction={<Link to="/destination"><Button variant="secondary">여행지 둘러보기</Button></Link>}
      />
    </Container>
  );
}
