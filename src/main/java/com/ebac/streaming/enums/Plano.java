package com.ebac.streaming.enums;

public enum Plano {
    BASIC(0), STANDARD(2), PREMIUM(4);

    private final int limiteVinculados;
    Plano(int limiteVinculados) { this.limiteVinculados = limiteVinculados; }

    public int limiteVinculados() { return limiteVinculados; }

    // a ordem BASIC < STANDARD < PREMIUM define a hierarquia de acesso
    public boolean permiteAcessoA(Plano planoMinimoDoFilme) {
        return this.ordinal() >= planoMinimoDoFilme.ordinal();
    }

    public boolean ehDowngradePara(Plano novo) {
        return novo.ordinal() < this.ordinal();
    }
}
