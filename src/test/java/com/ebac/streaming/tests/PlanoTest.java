package com.ebac.streaming.tests;

import com.ebac.streaming.enums.Plano;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlanoTest {
    @Test
    void limitesDeVinculadosPorPlano() {
        assertEquals(0, Plano.BASIC.limiteVinculados());
        assertEquals(2, Plano.STANDARD.limiteVinculados());
        assertEquals(4, Plano.PREMIUM.limiteVinculados());
    }
    @Test void premiumAcessaTodosOsPlanosMinimos() {
        assertTrue(Plano.PREMIUM.permiteAcessoA(Plano.BASIC));
        assertTrue(Plano.PREMIUM.permiteAcessoA(Plano.STANDARD));
        assertTrue(Plano.PREMIUM.permiteAcessoA(Plano.PREMIUM));
    }
    @Test void standardNaoAcessaFilmeExclusivoPremium() {
        assertFalse(Plano.STANDARD.permiteAcessoA(Plano.PREMIUM));
        assertTrue(Plano.STANDARD.permiteAcessoA(Plano.STANDARD));
    }
    @Test void basicSoAcessaFilmeBasic() {
        assertTrue(Plano.BASIC.permiteAcessoA(Plano.BASIC));
        assertFalse(Plano.BASIC.permiteAcessoA(Plano.STANDARD));
    }
    @Test void identificaDowngrade() {
        assertTrue(Plano.PREMIUM.ehDowngradePara(Plano.STANDARD));
        assertTrue(Plano.PREMIUM.ehDowngradePara(Plano.BASIC));
        assertTrue(Plano.STANDARD.ehDowngradePara(Plano.BASIC));
    }
    @Test void identificaUpgrade() {
        assertFalse(Plano.BASIC.ehDowngradePara(Plano.STANDARD));
        assertFalse(Plano.BASIC.ehDowngradePara(Plano.PREMIUM));
    }
}
