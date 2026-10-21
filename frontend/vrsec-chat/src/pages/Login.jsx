import { useState } from "react";
import "../styles/auth.css";
import { GoogleLogin } from "@react-oauth/google";
import { useNavigate } from "react-router-dom";

function readGoogleClaims(token) {
  try {
    const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = payload.padEnd(payload.length + ((4 - payload.length % 4) % 4), "=");
    return JSON.parse(atob(padded));
  } catch {
    return null;
  }
}

export default function Login() {
  const navigate = useNavigate();
  const [error, setError] = useState("");

  const handleSuccess = (credentialResponse) => {
    const token = credentialResponse?.credential;
    const claims = token ? readGoogleClaims(token) : null;
    const email = claims?.email?.trim().toLowerCase();

    // This check improves the login message; the backend verifies the token again.
    if (!token || !email || claims?.email_verified !== true || !email.endsWith("@vrsec.ac.in")) {
      localStorage.removeItem("googleToken");
      setError("Please login with your official college email (@vrsec.ac.in).");
      return;
    }

    setError("");
    localStorage.setItem("googleToken", token);
    navigate("/chat");
  };

  return (
    <div className="auth-container">
      <h1 className="bg-heading">Turn Hello's to Bonds...</h1>
      <div className="auth-card">
        <p className="auth-ticker">
          Sign in using your official college email (@vrsec.ac.in)
        </p>
        <h2>Oye</h2>
        <GoogleLogin
          theme="filled_blue"
          size="large"
          shape="pill"
          width="420"
          onSuccess={handleSuccess}
          onError={() => setError("Google sign-in failed. Please try again.")}
        />
      </div>

      {error && (
        <div className="auth-modal-backdrop" role="presentation" onClick={() => setError("")}>
          <section
            className="auth-error-modal"
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="auth-error-title"
            aria-describedby="auth-error-description"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="auth-error-icon" aria-hidden="true">!</div>
            <h2 id="auth-error-title">College email required</h2>
            <p id="auth-error-description">{error}</p>
            <p className="auth-error-hint">Use your official <strong>@vrsec.ac.in</strong> account to join Oye.</p>
            <button className="auth-error-button" type="button" onClick={() => setError("")}>
              Try again
            </button>
          </section>
        </div>
      )}
    </div>
  );
}
