package br.com.sistematenda.pdv.service;

import br.com.sistematenda.common.config.CaixaConfig;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class CaixaAutoCloseScheduler {

    @Inject
    SessaoCaixaService sessaoCaixaService;

    @Inject
    CaixaConfig caixaConfig;

    @Scheduled(every = "1m")
    void verificarFechamentoAutomatico() {
        try {
            String horaFechamento = caixaConfig.autoClose().hora();
            LocalTime agora = LocalTime.now();
            LocalTime alvo = LocalTime.parse(horaFechamento, DateTimeFormatter.ofPattern("HH:mm"));
            // Janela de 1 minuto a partir do horário configurado
            if (agora.getHour() == alvo.getHour() && agora.getMinute() == alvo.getMinute()) {
                int fechados = sessaoCaixaService.fecharTodosCaixasAbertos("SISTEMA_AUTO");
                if (fechados > 0) {
                    System.out.println("[CaixaAutoClose] Fechados automaticamente: " + fechados + " caixas às " + horaFechamento);
                }
            }
        } catch (Exception e) {
            System.err.println("[CaixaAutoClose] Erro: " + e.getMessage());
        }
    }
}