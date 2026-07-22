package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.DTO.AuthRequest;
import com.InventoryManagementSystem.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController
@RequestMapping("/auth")

public class AuthController
{

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest request)
    {
        if("admin".equals(request.username) && "admin123".equals(request.password))
        {
            return jwtUtil.generateToken(request.username);
        }
        return "Invalid Credentials";
    }
}