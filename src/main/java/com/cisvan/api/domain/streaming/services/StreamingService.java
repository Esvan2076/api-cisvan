package com.cisvan.api.domain.streaming.services;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.streaming.Streaming;
import com.cisvan.api.domain.streaming.StreamingRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StreamingService {

    private final StreamingRepository streamingRepository;
    
    public List<Streaming> getStreamings() {
        return streamingRepository.findAll();
    }

    public Optional<Streaming> getStreamingById(Integer id) {
        return streamingRepository.findById(id);
    }
}
