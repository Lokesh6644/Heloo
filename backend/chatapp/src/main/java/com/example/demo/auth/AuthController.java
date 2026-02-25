//package com.example.demo.auth;
//
//import com.example.demo.model.OtpRequest;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/auth")
//@CrossOrigin
//public class AuthController {
//
//    private final OtpService otpService;
//    private final JwtService jwtService;
//
//    public AuthController(OtpService otpService,
//                          JwtService jwtService) {
//        this.otpService = otpService;
//        this.jwtService = jwtService;
//    }
//
//    @PostMapping("/send-otp")
//    public String sendOtp(@RequestBody OtpRequest request) {
//
//        if (!request.getEmail().endsWith("@vrsec.ac.in")) {
//            return "Only VRSEC email allowed";
//        }
//
//        otpService.sendOtp(request.getEmail());
//        return "OTP sent";
//    }
//
//    @PostMapping("/verify-otp")
//    public String verifyOtp(@RequestBody OtpRequest request) {
//
//        boolean valid = otpService.verifyOtp(
//                request.getEmail(),
//                request.getOtp()
//        );
//
//        if (!valid) {
//            return "Invalid or Expired OTP";
//        }
//
//        // OTP already removed inside verifyOtp()
//
//        return jwtService.generateToken(request.getEmail());
//    }
//}