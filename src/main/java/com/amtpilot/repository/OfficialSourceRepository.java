package com.amtpilot.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amtpilot.entity.OfficialSource;

public interface OfficialSourceRepository
        extends JpaRepository<OfficialSource, UUID> {

    List<OfficialSource> findByCityIgnoreCaseOrderByTitleAsc(String city);
}