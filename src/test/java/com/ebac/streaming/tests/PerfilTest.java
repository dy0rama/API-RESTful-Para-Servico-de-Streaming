package com.ebac.streaming.tests;

import com.ebac.streaming.enums.Perfil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PerfilTest {
    @Test void idade11DeveSerInfantil() {
        assertEquals(Perfil.INFANTIL, Perfil.deIdade(11));
    }
    @Test
    void idade12DeveSerAdolescente() {
        assertEquals(Perfil.ADOLESCENTE, Perfil.deIdade(12));
    }
    @Test void idade17DeveSerAdolescente() {
        assertEquals(Perfil.ADOLESCENTE, Perfil.deIdade(17));
    }
    @Test void idade18DeveSerAdulto() {
        assertEquals(Perfil.ADULTO, Perfil.deIdade(18));
    }
    @Test void idadeNegativaDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () -> Perfil.deIdade(-55));
    }
}
