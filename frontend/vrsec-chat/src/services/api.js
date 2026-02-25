const BASE_URL = "http://localhost:8080";

export const sendOtp = async (email) => {
  const res = await fetch(`${BASE_URL}/auth/send-otp`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email })
  });

  return res.text();
};

export const verifyOtp = async (email, otp) => {
  const res = await fetch(`${BASE_URL}/auth/verify-otp`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, otp: parseInt(otp) })
  });

  return res.text(); // returns JWT
};