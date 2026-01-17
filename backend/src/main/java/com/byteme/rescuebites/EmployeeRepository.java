package com.byteme.rescuebites;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByUser_UserId(UUID userId);
    List<Employee> findByOrganisation_OrgId(UUID orgId);
}
