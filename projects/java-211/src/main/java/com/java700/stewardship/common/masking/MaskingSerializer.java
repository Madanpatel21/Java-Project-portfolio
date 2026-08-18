package com.java700.stewardship.common.masking;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * PHI masking at the API boundary. Names keep the first character plus asterisks;
 * MRNs keep the first two and last two characters so clinicians can verify identity
 * without full identifiers in transit logs.
 */
public class MaskingSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (value.length() >= 8) {
            gen.writeString(value.substring(0, 2) + "***" + value.substring(value.length() - 2));
        } else if (value.length() > 2) {
            gen.writeString(value.charAt(0) + "***");
        } else {
            gen.writeString("***");
        }
    }
}
