import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Navbar } from "@/components/commons/Navbar";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8000"; // Kong Gateway

export default function AuthPage() {
  const navigate = useNavigate();
  const [active, setActive] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Login form state
  const [loginData, setLoginData] = useState({ email: "", password: "" });

  // Register form state
  const [registerData, setRegisterData] = useState({
    username: "",
    email: "",
    password: "",
    role: "USER" // Default GlobalRole
  });

  // Handle OAuth2 redirect callback
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token");
    const refreshToken = params.get("refreshToken");
    const errorParam = params.get("error");

    if (token) {
      localStorage.setItem("accessToken", token);
      if (refreshToken) localStorage.setItem("refreshToken", refreshToken);

      // Optional: Perform a 'me' call to get user ID if not provided in URL
      // For now, we assume success and redirect
      navigate("/dashboard");
    } else if (errorParam) {
      setError("Social login failed: " + errorParam);
    }
  }, [navigate]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`${API_URL}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(loginData),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        let errorMessage = errorData.message || "Login failed";

        if (errorData.errors) {
          errorMessage = Object.values(errorData.errors).join(", ");
        }

        throw new Error(errorMessage);
      }

      const data = await response.json();

      // Store tokens
      localStorage.setItem("accessToken", data.accessToken || data.token);
      if (data.refreshToken) {
        localStorage.setItem("refreshToken", data.refreshToken);
      }
      if (data.user?.id) {
        localStorage.setItem("userId", data.user.id);
        // Store full user object (with plan) for fallbacks
        localStorage.setItem("user", JSON.stringify(data.user));
        sessionStorage.setItem("user", JSON.stringify(data.user));
      }
      if (data.tenantId) {
        localStorage.setItem("tenantId", data.tenantId);
      }

      // Redirect to dashboard
      navigate("/dashboard");
    } catch (err: any) {
      setError(err.message || "An error occurred during login");
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`${API_URL}/api/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(registerData),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        let errorMessage = errorData.message || "Registration failed";

        if (errorData.errors) {
          errorMessage = Object.values(errorData.errors).join("\n");
        }

        throw new Error(errorMessage);
      }

      // Registration successful - switch to login
      setActive(false);
      setLoginData({ email: registerData.email, password: registerData.password });
      setError(null);
      alert("Registration successful! Please login.");
    } catch (err: any) {
      setError(err.message || "An error occurred during registration");
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = `${API_URL}/api/auth/oauth2/authorization/google`;
  };

  return (
    <>
      <Navbar />
      <div className={`auth-container ${active ? "active" : ""}`}>
        {/* Error display */}
        {error && (
          <div
            style={{
              position: "absolute",
              top: "10px",
              left: "50%",
              transform: "translateX(-50%)",
              background: "#dc3545",
              color: "white",
              padding: "10px 20px",
              borderRadius: "8px",
              zIndex: 1000,
            }}
          >
            {error}
          </div>
        )}

        {/* LOGIN */}
        <div className="form-box login">
          <form onSubmit={handleLogin}>
            <h1>Login</h1>

            <div className="input-box">
              <input
                type="text"
                placeholder="Email or Username"
                required
                value={loginData.email}
                onChange={(e) =>
                  setLoginData({ ...loginData, email: e.target.value })
                }
              />
              <i className="bx bxs-user"></i>
            </div>

            <div className="input-box">
              <input
                type="password"
                placeholder="Password"
                required
                value={loginData.password}
                onChange={(e) =>
                  setLoginData({ ...loginData, password: e.target.value })
                }
              />
              <i className="bx bxs-lock-alt"></i>
            </div>

            <div className="forgot-link">
              <a href="#">Forgot Password?</a>
            </div>

            <button type="submit" className="btn" disabled={loading}>
              {loading ? "Logging in..." : "Login"}
            </button>

            <p>or login with social platforms</p>
            <div className="social-icons" style={{ display: 'flex', justifyContent: 'center', marginTop: '1rem', width: '100%' }}>
              <button
                onClick={(e) => { e.preventDefault(); handleGoogleLogin(); }}
                className="google-btn"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '10px',
                  background: 'white',
                  border: '1px solid #ddd',
                  borderRadius: '24px',
                  padding: '10px 24px',
                  cursor: 'pointer',
                  width: '100%',
                  maxWidth: '300px',
                  fontSize: '16px',
                  fontWeight: '500',
                  color: '#3c4043',
                  boxShadow: '0 1px 3px rgba(0,0,0,0.12)',
                  transition: 'background-color 0.2s'
                }}
                onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f8f9fa'}
                onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'white'}
                title="Login with Google"
              >
                <img
                  src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg"
                  alt="Google logo"
                  style={{ width: '18px', height: '18px' }}
                />
                Continue with Google
              </button>
            </div>
          </form>
        </div>

        {/* REGISTER */}
        <div className="form-box register">
          <form onSubmit={handleRegister}>
            <h1>Registration</h1>

            <div className="input-box">
              <input
                type="text"
                placeholder="Username"
                required
                value={registerData.username}
                onChange={(e) =>
                  setRegisterData({ ...registerData, username: e.target.value })
                }
              />
              <i className="bx bxs-user"></i>
            </div>

            <div className="input-box">
              <input
                type="email"
                placeholder="Email"
                required
                value={registerData.email}
                onChange={(e) =>
                  setRegisterData({ ...registerData, email: e.target.value })
                }
              />
              <i className="bx bxs-envelope"></i>
            </div>

            <div className="input-box">
              <input
                type="password"
                placeholder="Password"
                required
                value={registerData.password}
                onChange={(e) =>
                  setRegisterData({ ...registerData, password: e.target.value })
                }
              />
              <i className="bx bxs-lock-alt"></i>
            </div>

            <button type="submit" className="btn" disabled={loading}>
              {loading ? "Registering..." : "Register"}
            </button>

            <p>or register with social platforms</p>
            <div className="social-icons" style={{ display: 'flex', justifyContent: 'center', marginTop: '1rem', width: '100%' }}>
              <button
                onClick={(e) => { e.preventDefault(); handleGoogleLogin(); }}
                className="google-btn"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '10px',
                  background: 'white',
                  border: '1px solid #ddd',
                  borderRadius: '24px',
                  padding: '10px 24px',
                  cursor: 'pointer',
                  width: '100%',
                  maxWidth: '300px',
                  fontSize: '16px',
                  fontWeight: '500',
                  color: '#3c4043',
                  boxShadow: '0 1px 3px rgba(0,0,0,0.12)',
                  transition: 'background-color 0.2s'
                }}
                onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f8f9fa'}
                onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'white'}
                title="Register with Google"
              >
                <img
                  src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg"
                  alt="Google logo"
                  style={{ width: '18px', height: '18px' }}
                />
                Continue with Google
              </button>
            </div>
          </form>
        </div>

        {/* TOGGLE */}
        <div className="toggle-box">
          <div className="toggle-panel toggle-left">
            <h1>Welcome Back!</h1>
            <p>Don't have an account?</p>

            <button
              className="btn register-btn"
              type="button"
              onClick={() => setActive(true)}
            >
              Register
            </button>
          </div>

          <div className="toggle-panel toggle-right">
            <h1>Hello, Welcome!</h1>
            <p>Already have an account?</p>

            <button
              className="btn login-btn"
              type="button"
              onClick={() => setActive(false)}
            >
              Login
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
