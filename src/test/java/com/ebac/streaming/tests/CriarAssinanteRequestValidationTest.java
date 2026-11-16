package com.ebac.streaming.tests;

import com.ebac.streaming.enums.Plano;
import com.ebac.streaming.requests.CriarAssinanteRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CriarAssinanteRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test void assinanteMenorDe18DeveSerInvalido() {
        var r = new CriarAssinanteRequest("Ana", "ana@x.com", 17, Plano.BASIC, "senha123");
        assertFalse(validator.validate(r).isEmpty());
    }
    @Test
    void idadeNegativaDeveSerInvalida() {
        var r = new CriarAssinanteRequest("Ana", "ana@x.com", -55, Plano.BASIC, "senha123");
        assertFalse(validator.validate(r).isEmpty());
    }
    @Test void payloadValidoNaoTemErros() {
        var r = new CriarAssinanteRequest("Ana", "ana@x.com", 18, Plano.BASIC, "senha123");
        assertTrue(validator.validate(r).isEmpty());
    }
}
