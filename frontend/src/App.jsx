import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage/LoginPage.jsx'
import RegisterPage from './pages/RegisterPage/RegisterPage.jsx'
import OTPPage from './pages/OTPPage/OTPPage.jsx'
import ForgotPasswordPage from './pages/ForgotPasswordPage/ForgotPasswordPage.jsx'
import ResetPasswordPage from './pages/ResetPasswordPage/ResetPasswordPage.jsx'
import OnboardingPage from './pages/OnboardingPage/OnboardingPage.jsx'
import MapPage from './pages/MapPage/MapPage.jsx'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/otp" element={<OTPPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="/onboarding" element={<OnboardingPage />} />
      <Route path="/map" element={<MapPage />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
