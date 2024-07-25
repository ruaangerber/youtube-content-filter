package com.awesome.filter.service.service;

import com.awesome.filter.service.model.request.FilterRequest;
import com.awesome.filter.service.model.response.FilterResponse;

public interface FilterService {
    
    FilterResponse filter(FilterRequest request);

}
