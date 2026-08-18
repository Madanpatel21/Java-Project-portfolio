package com.java700.crvs.verification;


import com.java700.crvs.common.api.Problems;
import com.java700.crvs.registry.NationalIdGenerator;
import com.java700.crvs.registry.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Third-party identity verification (banks, employers, KYC providers). Deceased status is
 * propagated instantly: a death registration approval immediately changes what verifiers see,
 * which is the registry's core defense against post-mortem identity fraud.
 */
@Service
public class VerificationService {

    private final PersonRepository persons;

    public VerificationService(PersonRepository persons) {
        this.persons = persons;
    }

    @Transactional(readOnly = true)
    public VerificationApi.PersonVerification verify(String nationalId) {
        if (!NationalIdGenerator.isValid(nationalId)) {
            throw new Problems.BadRequest("Invalid national id format");
        }
        var person = persons.findByNationalId(nationalId)
                .orElseThrow(() -> new Problems.NotFound("Person not found"));
        return new VerificationApi.PersonVerification(person.getNationalId(), true,
                person.getStatus().name(), person.getFullName(), person.getDob().toString(),
                person.getRegion(),
                person.getDeceasedAt() == null ? null : person.getDeceasedAt().toString());
    }
}
