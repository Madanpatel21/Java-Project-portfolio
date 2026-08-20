package com.java700.p2p.common.masking;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * Identity masking at the API boundary: keeps first two and last two characters so
 * operators can verify identity without full identifiers in logs and listings.
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
