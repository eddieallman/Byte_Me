package com.byteme.rescuebites;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EmployeeBadgeRepository extends JpaRepository<EmployeeBadge, EmployeeBadge.Key> {
    List<EmployeeBadge> findByEmployeeId(UUID employeeId);
    boolean existsByEmployeeIdAndBadgeId(UUID employeeId, UUID badgeId);
}
