package org.cloudfoundry.samples.music.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.util.Util;

@RestController
@RequestMapping(value = "/calendar")
public class CalendarController {
    public CalendarController() {}

    @RequestMapping(value = "/getDatesForMonth", method = RequestMethod.GET)
    public String getShiftsForOfMonth(@RequestParam("month") String month) {
        return Util.getDatesForMonth(Integer.parseInt(month)).toString();
    }
}