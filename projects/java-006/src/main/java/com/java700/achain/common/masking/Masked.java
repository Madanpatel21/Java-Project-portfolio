package com.java700.achain.common.masking;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks identity fields that must be masked in list/search API responses.
 * Certificates and verification responses disclose identity by design (that is their
 * legal purpose); everything else is least-disclosure.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = MaskingSerializer.class)
public @interface Masked {
}
