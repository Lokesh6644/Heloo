import { GoogleLogin } from "@react-oauth/google";
import { useNavigate } from "react-router-dom";

export default function Login() {

  const navigate = useNavigate();

  const handleLoginSuccess = (credentialResponse) => {

    if (!credentialResponse?.credential) {
      console.error("No credential received");
      return;
    }

    // Store token
    localStorage.setItem("googleToken", credentialResponse.credential);

    // Redirect to chat
    navigate("/chat");
  };

  const handleLoginError = () => {
    console.error("Google Login Failed");
  };

  return (
    <div className="auth-container">

      <h1 className="bg-heading">
        Turning Hello's into Friendships...
      </h1>

      <div className="auth-card">

        <p className="auth-ticker">
          <span>
            🚨 Please sign in using your official college email (@vrsec.ac.in). 
            Other email domains will be restricted.
          </span>
        </p>

        <h2>Oye</h2>

        <GoogleLogin
          onSuccess={handleLoginSuccess}
          onError={handleLoginError}
          theme="filled_blue"
          size="large"
          shape="pill"
          width="420"
        />

      </div>
    </div>
  );
}