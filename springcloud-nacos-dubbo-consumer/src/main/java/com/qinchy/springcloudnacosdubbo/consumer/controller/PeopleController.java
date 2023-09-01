package com.qinchy.springcloudnacosdubbo.consumer.controller;

import com.qinchy.springcloudnacosdubbo.api.PeopleService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/people")
public class PeopleController {

    @DubboReference(version = "1.0")
    private PeopleService peopleService;

    @GetMapping("/selectAll")
    public List<String> selectAll() {
        return peopleService.selectAll();
    }
}
