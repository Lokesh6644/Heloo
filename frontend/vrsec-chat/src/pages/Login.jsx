import { GoogleLogin } from "@react-oauth/google";
import { jwtDecode } from "jwt-decode";
import { useNavigate } from "react-router-dom";

export default function Login() {

  const navigate = useNavigate();

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>Sign in with College Email</h2>

        <GoogleLogin
          onSuccess={credentialResponse => {

            const decoded = jwtDecode(credentialResponse.credential);

            const email = decoded.email;

            if (!email.endsWith("@vrsec.ac.in")) {
              alert("Only college emails allowed");
              return;
            }

            localStorage.setItem("googleToken", credentialResponse.credential);

            navigate("/chat");
          }}
          onError={() => {
            console.log("Login Failed");
          }}
        />

      </div>
    </div>
  );
}