import { useState } from "react";
import { verifyOtp } from "../services/api";
import { useLocation, useNavigate } from "react-router-dom";
import "../styles/auth.css";

export default function Otp() {

  const [otp, setOtp] = useState("");
  const location = useLocation();
  const navigate = useNavigate();

  const email = location.state?.email;

  const handleVerify = async () => {
    const token = await verifyOtp(email, otp);

    localStorage.setItem("authToken", token);

    navigate("/chat");
  };

  

return (
  <div className="auth-container">
    <div className="auth-card">
      <h2>Verify OTP</h2>

      <input
        className="auth-input"
        value={otp}
        onChange={(e) => setOtp(e.target.value)}
        placeholder="Enter OTP"
      />

      <button className="auth-button" onClick={handleVerify}>
        Verify & Continue
      </button>
    </div>
  </div>
);
}