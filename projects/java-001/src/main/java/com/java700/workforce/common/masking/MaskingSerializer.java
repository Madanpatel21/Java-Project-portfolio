package com.java700.workforce.common.masking;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * PII masking for API responses. E-mails keep the first character and the domain;
 * generic strings keep the first two characters. Never used for persistence or
 * evidence payloads (evidence stores the full, unmasked record).
 */
public class MaskingSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        int at = value.indexOf('@');
        if (at > 1) {
            gen.writeString(value.charAt(0) + "***@" + value.substring(at + 1));
        } else if (value.length() > 4) {
            gen.writeString(value.substring(0, 2) + "***");
        } else {
            gen.writeString("***");
        }
    }
}
