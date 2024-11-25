package com.example.bpmanagement.Repository;

import com.example.bpmanagement.Entity.BoardFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardFileRepository extends JpaRepository<BoardFile, Long> {
}