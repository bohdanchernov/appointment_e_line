package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    AppointmentNotifyBot bot;

    @GetMapping("/nieFound/{nie}")
    @ResponseBody
    public String nieFound(@PathVariable String nie) {
        bot.nieFound(nie);
        return "Receiver is successfully notified";
    }
}
