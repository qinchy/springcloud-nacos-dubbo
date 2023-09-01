package com.qinchy.springcloudnacosdubbo.provider.service;

import com.qinchy.springcloudnacosdubbo.api.PeopleService;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.ArrayList;
import java.util.List;

@DubboService(version = "1.0")
public class PeopleServiceImpl implements PeopleService {
    @Override
    public List<String> selectAll() {
        return new ArrayList<String>() {{
            add("Tom");
            add("Jerry");
        }};
    }
}
