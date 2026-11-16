package com.ebac.streaming.services;

import org.springframework.stereotype.Component;

@Component
public class SimuladorReproducaoReal implements SimuladorReproducao {
    @Override
    public void reproduzir(int segundos) {
        try {
            Thread.sleep(segundos * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Reprodução interrompida");
        }
    }
}
