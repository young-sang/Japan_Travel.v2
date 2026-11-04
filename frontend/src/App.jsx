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
import News from './pages/News.jsx';
import NewsDetail from './pages/NewsDetail.jsx';
import NewsAdmin from './pages/admin/NewsAdmin.jsx';
import NewsEditor from './pages/admin/NewsEditor.jsx';
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
            {/* 옛 관리자 화면(D-009) 은 부를 API 가 없어 라우트에서 뺐다 (D-054). 파일은 남아 있다. 게시판은 D-061 로 삭제 */}
            <Route path="/destination" element={<Destination />} />
            <Route path="/festival" element={<Festival />} />
            <Route path="/course" element={<Course />} />
            <Route path="/top25" element={<Top25 />} />
            {/* 옛 주소 — Top100 은 Top25 로 바뀌었다 (D-076) */}
            <Route path="/top100" element={<Navigate to="/top25" replace />} />
            {/* 뉴스는 관리자가 직접 쓴다 (D-075 · D-079) */}
            <Route path="/news" element={<News />} />
            <Route path="/news/:id" element={<NewsDetail />} />
            <Route path="/admin" element={<Navigate to="/admin/news" replace />} />
            <Route path="/admin/news" element={<ProtectedRoute requireAdmin><NewsAdmin /></ProtectedRoute>} />
            <Route path="/admin/news/new" element={<ProtectedRoute requireAdmin><NewsEditor /></ProtectedRoute>} />
            <Route path="/admin/news/:id/edit" element={<ProtectedRoute requireAdmin><NewsEditor /></ProtectedRoute>} />
            <Route path="/tip" element={<Tip />} />
            <Route path="/tip/:slug" element={<TipDetail />} />
            <Route path="/detail/:type/:id" element={<Detail />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/mypage" element={<Navigate to="/mypage/favorites" replace />} />
            <Route path="/mypage/:tab" element={<ProtectedRoute><MyPage /></ProtectedRoute>} />
            <Route path="/mypage/courses/new" element={<ProtectedRoute><CourseBuilder /></ProtectedRoute>} />
            <Route path="/mypage/courses/:id/edit" element={<ProtectedRoute><CourseBuilder /></ProtectedRoute>} />
            {/* 삭제한 게시판(/board) 같은 옛 링크도 여기로. 홈으로 몰래 보내지 않는다 (D-068) */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </ErrorBoundary>
      </main>
      <Footer />
    </ToastProvider>
  );
}
