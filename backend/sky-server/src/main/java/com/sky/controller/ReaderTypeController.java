package com.sky.controller;

import com.sky.entity.ReaderType;
import com.sky.result.Result;
import com.sky.service.ReaderTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reader-types")
public class ReaderTypeController {

    @Autowired
    private ReaderTypeService readerTypeService;

    @GetMapping
    public Result<List<ReaderType>> listEnabled() {
        return Result.success(readerTypeService.listEnabled());
    }
}

