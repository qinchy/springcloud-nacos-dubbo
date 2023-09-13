package com.qinchy.springcloudnacosdubbo.provider.service;

import com.qinchy.springcloudnacosdubbo.api.PeopleService;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.ArrayList;
import java.util.List;

@DubboService(version = "1.0")
public class PeopleServiceImpl implements PeopleService {

    private static final List<String> RESULT = new ArrayList<String>(){{
        add("Tom");
        add("Jerry");
    }};

    // 改成访问final防止频繁GC
    @Override
    public List<String> selectAll() {
        return RESULT;
    }
}
