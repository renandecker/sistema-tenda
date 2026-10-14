package br.com.sistematenda.common.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "app")
public interface AppConfig {

    @WithDefault("Sistema Tenda")
    String name();

    @WithDefault("1.0.0")
    String version();

    @WithDefault("America/Sao_Paulo")
    String timezone();

    @WithDefault("BRL")
    String currency();

    @WithDefault("pt_BR")
    String locale();
}
