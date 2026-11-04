import { Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header.jsx';
import Footer from './components/Footer.jsx';
import Home from './pages/Home.jsx';
import Destination from './pages/Destination.jsx';
import Festival from './pages/Festival.jsx';
import Course from './pages/Course.jsx';
import Detail from './pages/Detail.jsx';
import Tip from './pages/Tip.jsx';
import TipDetail from './pages/TipDetail.jsx';
import Top25 from './pages/Top25.jsx';
import MyPage from './pages/MyPage.jsx';
import CourseBuilder from './pages/CourseBuilder.jsx';
import Login from './pages/Login.jsx';
import Signup from './pages/Signup.jsx';
import NotFound from './pages/NotFound.jsx';
import ProtectedRoute from './auth/ProtectedRoute.jsx';
import ErrorBoundary from './components/ui/ErrorBoundary.jsx';
import ScrollToTop from './components/ui/ScrollToTop.jsx';
import { ToastProvider } from './components/ui/Toast.jsx';

export default function App() {
  return (
    <ToastProvider>
      <ScrollToTop />
      <Header />
      <main id="main">
        <ErrorBoundary>
          <Routes>
            <Route path="/" element={<Home />} />
            {/* 관리자(D-009) 는 부를 API 가 없어 라우트에서 뺐다 (D-054). 파일은 남아 있다. 게시판은 D-061 로 삭제.
                뉴스는 관리자가 직접 쓰는 방식(D-075)이 생길 때까지 숨긴다 (D-073) */}
            <Route path="/destination" element={<Destination />} />
            <Route path="/festival" element={<Festival />} />
            <Route path="/course" element={<Course />} />
            <Route path="/top25" element={<Top25 />} />
            {/* 옛 주소 — Top100 은 Top25 로 바뀌었다 (D-076) */}
            <Route path="/top100" element={<Navigate to="/top25" replace />} />
            <Route path="/tip" element={<Tip />} />
            <Route path="/tip/:slug" element={<TipDetail />} />
            <Route path="/detail/:type/:id" element={<Detail />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/mypage" element={<Navigate to="/mypage/favorites" replace />} />
            <Route path="/mypage/:tab" element={<ProtectedRoute><MyPage /></ProtectedRoute>} />
            <Route path="/mypage/courses/new" element={<ProtectedRoute><CourseBuilder /></ProtectedRoute>} />
            <Route path="/mypage/courses/:id/edit" element={<ProtectedRoute><CourseBuilder /></ProtectedRoute>} />
            {/* 숨긴 화면(/admin) · 삭제한 게시판(/board) 의 옛 링크도 여기로. 홈으로 몰래 보내지 않는다 (D-068) */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </ErrorBoundary>
      </main>
      <Footer />
    </ToastProvider>
  );
}
