package com.awesome.filter.service.service;

import java.util.List;

import com.awesome.filter.service.model.internal.FilteredText;

public interface WordlistService {
    
    List<FilteredText> match(final String text);

}
