package br.com.sistematenda.pdv.tef;

import java.time.LocalDateTime;
import java.util.Map;

public class TefEventoResponse {

    public String requestId;
    public String evento;
    public String mensagem;
    public Map<String, Object> dados;
    public LocalDateTime timestamp;
}