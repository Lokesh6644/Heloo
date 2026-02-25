import { GoogleLogin } from "@react-oauth/google";
//import { jwtDecode } from "jwt-decode";
//import { useNavigate } from "react-router-dom";

export default function Login() {

 // const navigate = useNavigate();

  return (
    <div className="auth-container">
      <h1 className="bg-heading">
    Turning Hello's into Friendships...
  </h1>
      <div className="auth-card">

   

<p className="auth-ticker">
  <span>
    🚨 Please sign in using your official college email (@vrsec.ac.in). 
    Other email domains will be restricted from accessing VRSEC Campus Chat.
  </span>
</p>

        <h2> Oye</h2>

         <GoogleLogin
    theme="filled_blue"
    size="large"
    shape="pill"
    width="420"
  />
      
      </div>
    </div>
  );
}