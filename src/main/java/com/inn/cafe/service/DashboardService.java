package com.inn.cafe.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public interface DashboardService {
    ResponseEntity<Map<String, Object>> getCount();

}
