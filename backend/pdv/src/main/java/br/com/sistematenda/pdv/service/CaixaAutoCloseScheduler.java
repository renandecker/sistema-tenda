package br.com.sistematenda.pdv.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class CaixaAutoCloseScheduler {

    @Inject
    SessaoCaixaService sessaoCaixaService;

    @ConfigProperty(name = "caixa.auto-close.hora", defaultValue = "23:30")
    String horaFechamento;

    @Scheduled(cron = "0 0 23 * * *")
    void verificarFechamentoAutomatico() {
        try {
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