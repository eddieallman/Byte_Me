package com.byteme.rescuebites;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface RescueEventRepository extends JpaRepository<RescueEvent, UUID> {
    
    List<RescueEvent> findByEmployee_EmployeeId(UUID employeeId);
    
    long countByEmployee_EmployeeId(UUID employeeId);
    
    @Query("SELECT COALESCE(SUM(r.co2eEstimateGrams), 0) FROM RescueEvent r WHERE r.employee.employeeId = :employeeId")
    long sumCo2eByEmployee(UUID employeeId);
    
    @Query("SELECT COALESCE(SUM(r.mealsEstimate), 0) FROM RescueEvent r WHERE r.employee.employeeId = :employeeId")
    long sumMealsByEmployee(UUID employeeId);
}
