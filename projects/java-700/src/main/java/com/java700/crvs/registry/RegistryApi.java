package com.java700.crvs.registry;

import com.java700.crvs.common.masking.Masked;
import java.time.Instant;
import java.time.LocalDate;

public final class RegistryApi {

    private RegistryApi() {
    }

    public record PersonView(String id, @Masked String nationalId, @Masked String fullName,
                             LocalDate dob, String sex, String region, String status,
                             Instant registeredAt, Instant deceasedAt) {

        static PersonView from(Person p) {
            return new PersonView(p.getId(), p.getNationalId(), p.getFullName(), p.getDob(),
                    p.getSex(), p.getRegion(), p.getStatus().name(), p.getRegisteredAt(),
                    p.getDeceasedAt());
        }
    }

    public record OfficeView(String id, String code, String name, String region) {

        static OfficeView from(Office o) {
            return new OfficeView(o.getId(), o.getCode(), o.getName(), o.getRegion());
        }
    }
}
