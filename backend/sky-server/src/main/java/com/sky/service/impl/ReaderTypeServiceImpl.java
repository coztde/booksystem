package com.sky.service.impl;

import com.sky.entity.ReaderType;
import com.sky.mapper.ReaderTypeMapper;
import com.sky.service.ReaderTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReaderTypeServiceImpl implements ReaderTypeService {

    @Autowired
    private ReaderTypeMapper readerTypeMapper;

    @Override
    public List<ReaderType> listEnabled() {
        return readerTypeMapper.listEnabled();
    }
}

