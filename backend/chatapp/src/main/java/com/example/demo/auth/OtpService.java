//package com.example.demo.auth;
//
////package com.vrsec.chatapp.auth;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//import java.util.Random;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Service
//public class OtpService {
//
//    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    public void sendOtp(String email) {
//
//        if (!email.endsWith("@vrsec.ac.in")) {
//            throw new RuntimeException("Only college emails allowed");
//        }
//
//        int otp = new Random().nextInt(900000) + 100000;
//
//        long expiry = System.currentTimeMillis() + 5 * 60 * 1000; // 5 minutes
//
//        otpStore.put(email, new OtpData(otp, expiry));
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        // ****** for testing purpose..........
//        message.setFrom("lokeshkiliki@gmail.com");  // ADD THIS LINE
//        message.setTo(email);
//        message.setSubject("VRSEC Anonymous Chat - OTP");
//        message.setText("Your OTP is: " + otp + "\nValid for 5 minutes.");
//
//       // System.out.println("Sending OTP to: " + email);
//      //  System.out.println("OTP generated: " + otp);
//
//
//
//      //  ********* //for a  while keep this
////        try {
////            mailSender.send(message);
////            System.out.println("Mail sent successfully");
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
//
//        //************ upto to this
//        mailSender.send(message);
//    }
//
//    public boolean verifyOtp(String email, int enteredOtp) {
//
//        OtpData data = otpStore.get(email);
//
//        if (data == null) return false;
//
//        if (System.currentTimeMillis() > data.expiryTime) {
//            otpStore.remove(email);
//            return false;
//        }
//
//        if (data.otp == enteredOtp) {
//            otpStore.remove(email);
//            return true;
//        }
//
//        return false;
//    }
//
//    private static class OtpData {
//        int otp;
//        long expiryTime;
//
//        OtpData(int otp, long expiryTime) {
//            this.otp = otp;
//            this.expiryTime = expiryTime;
//        }
//    }
//}