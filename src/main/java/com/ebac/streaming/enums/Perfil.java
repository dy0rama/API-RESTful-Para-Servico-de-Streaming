package com.ebac.streaming.enums;

public enum Perfil {
    INFANTIL, ADOLESCENTE, ADULTO;

    public static Perfil deIdade(int idade) {
        if (idade < 0) throw new IllegalArgumentException("Idade inválida: " + idade);
        if (idade < 12) return INFANTIL;
        if (idade < 18) return ADOLESCENTE;
        return ADULTO;
    }

    public boolean permite(ClassificacaoEtaria classificacao, boolean permissaoAdulto) {
        return switch (this) {
            case INFANTIL -> classificacao.anos() <= 12;
            case ADOLESCENTE -> classificacao.anos() <= 16
                    || (classificacao == ClassificacaoEtaria.DEZOITO && permissaoAdulto);
            case ADULTO -> true;
        };
    }
}
