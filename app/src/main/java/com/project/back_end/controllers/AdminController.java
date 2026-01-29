
package com.project.back_end.controllers;
import com.project.back_end.models.Admin;
import com.project.back_end.services.ClinicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.path}" + "admin")
public class AdminController {
    private final ClinicService clinicService;

    public AdminController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        return clinicService.validateAdmin(admin);
    }

}

