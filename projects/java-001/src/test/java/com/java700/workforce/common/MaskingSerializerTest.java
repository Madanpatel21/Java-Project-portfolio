package com.java700.workforce.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.workforce.common.masking.Masked;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingSerializerTest {

    record Person(String id, @Masked String email, @Masked String phone) {
    }

    @Test
    void masksEmailAndGenericStrings() throws Exception {
        String json = new ObjectMapper().writeValueAsString(
                new Person("p1", "alice@corp.example", "00491511234567"));
        assertThat(json).contains("a***@corp.example");
        assertThat(json).doesNotContain("alice@corp.example");
        assertThat(json).doesNotContain("00491511234567");
        assertThat(json).contains("\"id\":\"p1\"");
    }
}
