package com.amtpilot.authority.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amtpilot.entity.Authority;
import com.amtpilot.repository.AuthorityRepository;

@Service
public class AuthorityService {

    private final AuthorityRepository authorityRepository;

    public AuthorityService(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    @Transactional(readOnly = true)
    public List<Authority> findByCity(String city) {
        return authorityRepository
                .findByCityIgnoreCaseOrderByNameAsc(city.trim());
    }
}