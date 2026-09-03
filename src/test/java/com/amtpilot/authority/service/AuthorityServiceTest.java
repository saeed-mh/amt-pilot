package com.amtpilot.authority.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amtpilot.entity.Authority;
import com.amtpilot.repository.AuthorityRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorityServiceTest {

    @Mock
    private AuthorityRepository authorityRepository;

    @InjectMocks
    private AuthorityService authorityService;

    @Test
    void returnsAuthoritiesForTrimmedCity() {
        Authority authority = new Authority(
                "Immigration Office",
                "IMMIGRATION",
                "Dortmund",
                "https://example.com/immigration",
                null);

        List<Authority> expectedAuthorities = List.of(authority);

        when(authorityRepository
                .findByCityIgnoreCaseOrderByNameAsc("Dortmund"))
                .thenReturn(expectedAuthorities);

        List<Authority> result =
                authorityService.findByCity(" Dortmund ");

        assertThat(result).isSameAs(expectedAuthorities);

        verify(authorityRepository)
                .findByCityIgnoreCaseOrderByNameAsc("Dortmund");
    }
}