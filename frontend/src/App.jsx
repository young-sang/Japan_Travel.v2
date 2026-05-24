import { Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header.jsx';
import Footer from './components/Footer.jsx';
import Home from './pages/Home.jsx';
import Theme from './pages/Theme.jsx';
import Destination from './pages/Destination.jsx';
import Festival from './pages/Festival.jsx';
import Course from './pages/Course.jsx';
import Detail from './pages/Detail.jsx';
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
            <Route path="/theme" element={<Theme />} />
            <Route path="/destination" element={<Destination />} />
            <Route path="/festival" element={<Festival />} />
            <Route path="/course" element={<Course />} />
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
