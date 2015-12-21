package org.beyondblog.controller;

import org.beyondblog.model.Hello;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/*******************************************************************************
 * Copyright (c) 2005-2016 ****, Inc.
 * HelloController
 * Contributors:
 * beyondblog  on 12/20/15 - 8:50 AM
 *******************************************************************************/
@Controller
@RequestMapping("/")
public class HelloController {

    @Value("${app.profile}")
    private String profile;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    @ResponseBody
    public Object hello() {
        Hello hello = new Hello();
        //--spring.profiles.active=dev
        hello.setCode(200);
        hello.setMessge("Hello spring! profile=" + profile);
        return hello;
    }
}
