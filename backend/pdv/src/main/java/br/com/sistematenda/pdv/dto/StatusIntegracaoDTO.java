package br.com.sistematenda.pdv.dto;

import lombok.Data;

/**
 * Resultado de um teste de integração de produção (TEF, PIX ou impressora).
 *
 * <p>Os status seguem o mesmo vocabulário usado no frontend:</p>
 * <ul>
 *   <li>{@code ok} — comunicação real confirmada;</li>
 *   <li>{@code atencao} — respondeu, mas há uma ressalva (ex.: simulador);</li>
 *   <li>{@code erro} — não foi possível comunicar;</li>
 *   <li>{@code nao_configurado} — nenhum destino/provedor configurado;</li>
 *   <li>{@code pendente} — configurado, porém ainda não testado.</li>
 * </ul>
 */
@Data
public class StatusIntegracaoDTO {

    private String componente;
    private String status;
    private String mensagem;
    private String detalhe;
    private String destino;
    private Long tempoMs;
    private String horaVerificacao;
}
