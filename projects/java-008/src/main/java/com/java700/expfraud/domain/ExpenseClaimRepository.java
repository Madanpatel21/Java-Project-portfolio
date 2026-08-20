package com.java700.expfraud.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Expense claim persistence. */
public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim, String> {

    Optional<ExpenseClaim> findByClaimNo(String claimNo);

    List<ExpenseClaim> findByStatusOrderBySubmittedAtAsc(String status);

    List<ExpenseClaim> findByStatusInOrderBySubmittedAtAsc(List<String> statuses);

    long countByStatus(String status);

    List<ExpenseClaim> findByStatusInAndRiskScoreGreaterThanEqual(List<String> statuses, int minScore);

    List<ExpenseClaim> findByDepartmentAndCategoryAndStatusInAndExpenseDateAfter(
            String department, String category, List<String> statuses, LocalDate after);

    List<ExpenseClaim> findByMerchantAndExpenseDateAfter(String merchant, LocalDate after);

    List<ExpenseClaim> findByEmployeeIdAndExpenseDateAfter(String employeeId, LocalDate after);
}
