package com.cisvan.api.component.streaming;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StreamingService {
    @Autowired
    private StreamingRepository streamingRepository;
    
    public List<Streaming> findAll() {
        return streamingRepository.findAll();
    }

    public Optional<Streaming> findById(Integer id) {
        return streamingRepository.findById(id);
    }
}
