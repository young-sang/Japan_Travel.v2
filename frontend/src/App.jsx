import { Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header.jsx';
import Footer from './components/Footer.jsx';
import Home from './pages/Home.jsx';
import Board from './pages/Board.jsx';
import PostDetail from './pages/PostDetail.jsx';
import PostEditor from './pages/PostEditor.jsx';
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
import Admin from './pages/Admin.jsx';
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
            <Route path="/board" element={<Board />} />
            <Route path="/board/new" element={<ProtectedRoute><PostEditor /></ProtectedRoute>} />
            <Route path="/board/:id" element={<PostDetail />} />
            <Route path="/board/:id/edit" element={<ProtectedRoute><PostEditor /></ProtectedRoute>} />
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
            <Route path="/mypage/:tab" element={<ProtectedRoute denyAdmin><MyPage /></ProtectedRoute>} />
            <Route path="/mypage/courses/new" element={<ProtectedRoute denyAdmin><CourseBuilder /></ProtectedRoute>} />
            <Route path="/mypage/courses/:id/edit" element={<ProtectedRoute denyAdmin><CourseBuilder /></ProtectedRoute>} />
            <Route path="/admin/*" element={<ProtectedRoute requireAdmin><Admin /></ProtectedRoute>} />
          </Routes>
        </ErrorBoundary>
      </main>
      <Footer />
    </ToastProvider>
  );
}
