import { Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header.jsx';
import Footer from './components/Footer.jsx';
import Home from './pages/Home.jsx';
import Destination from './pages/Destination.jsx';
import Festival from './pages/Festival.jsx';
import Course from './pages/Course.jsx';
import Detail from './pages/Detail.jsx';
import Hotplace from './pages/Hotplace.jsx';
import News from './pages/News.jsx';
import Tip from './pages/Tip.jsx';
import TipDetail from './pages/TipDetail.jsx';
import Top100 from './pages/Top100.jsx';
import MyPage from './pages/MyPage.jsx';
import CourseBuilder from './pages/CourseBuilder.jsx';
import Login from './pages/Login.jsx';
import Signup from './pages/Signup.jsx';
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
            {/* 관리자(D-009) 는 부를 API 가 없어 라우트에서 뺐다 (D-054). 파일은 남아 있다. 게시판은 D-061 로 삭제 */}
            <Route path="/destination" element={<Destination />} />
            <Route path="/festival" element={<Festival />} />
            <Route path="/course" element={<Course />} />
            <Route path="/top100" element={<Top100 />} />
            <Route path="/hotplace" element={<Hotplace />} />
            <Route path="/news" element={<News />} />
            <Route path="/tip" element={<Tip />} />
            <Route path="/tip/:slug" element={<TipDetail />} />
            <Route path="/detail/:type/:id" element={<Detail />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/mypage" element={<Navigate to="/mypage/favorites" replace />} />
            <Route path="/mypage/:tab" element={<ProtectedRoute><MyPage /></ProtectedRoute>} />
            <Route path="/mypage/courses/new" element={<ProtectedRoute><CourseBuilder /></ProtectedRoute>} />
            <Route path="/mypage/courses/:id/edit" element={<ProtectedRoute><CourseBuilder /></ProtectedRoute>} />
            {/* 숨긴 화면(/admin) 이나 삭제한 게시판(/board)의 옛 링크나 북마크로 들어와도 빈 화면 대신 홈으로 */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </ErrorBoundary>
      </main>
      <Footer />
    </ToastProvider>
  );
}
