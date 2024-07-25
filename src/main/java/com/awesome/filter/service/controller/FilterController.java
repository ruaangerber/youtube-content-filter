package com.awesome.filter.service.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.awesome.filter.service.model.request.FilterRequest;
import com.awesome.filter.service.model.response.FilterResponse;
import com.awesome.filter.service.service.FilterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FilterController {

    private final FilterService filterService;

    @PostMapping(
        value = "/filter",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )    
    public FilterResponse filter(@RequestBody final FilterRequest request) {
        return filterService.filter(request);
    }

}
