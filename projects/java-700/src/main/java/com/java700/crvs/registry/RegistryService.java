package com.java700.crvs.registry;


import com.java700.crvs.common.api.PageResponse;
import com.java700.crvs.common.api.Problems;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistryService {

    private final PersonRepository personRepository;

    public RegistryService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Transactional(readOnly = true)
    public Person get(String id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Person not found"));
    }

    @Transactional(readOnly = true)
    public Person byNationalId(String nationalId) {
        return personRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new Problems.NotFound("Person not found"));
    }

    @Transactional(readOnly = true)
    public PageResponse<RegistryApi.PersonView> search(String query, int page, int size) {
        String q = query == null ? "" : query;
        return PageResponse.from(personRepository
                .findByFullNameContainingIgnoreCase(q, PageRequest.of(page, Math.min(size, 100),
                        Sort.by("fullName")))
                .map(RegistryApi.PersonView::from));
    }
}
