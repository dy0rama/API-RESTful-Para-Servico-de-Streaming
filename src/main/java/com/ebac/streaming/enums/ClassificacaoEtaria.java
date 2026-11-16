package com.ebac.streaming.enums;

public enum ClassificacaoEtaria {
    LIVRE(0), DOZE(12), DEZESSEIS(16), DEZOITO(18);

    private final int anos;
    ClassificacaoEtaria(int anos) { this.anos = anos; }
    public int anos() { return anos; }
}
