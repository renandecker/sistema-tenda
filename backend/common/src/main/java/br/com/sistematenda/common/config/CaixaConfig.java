package br.com.sistematenda.common.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "caixa")
public interface CaixaConfig {

    @WithName("auto-close")
    AutoClose autoClose();

    interface AutoClose {

        @WithDefault("23:30")
        String hora();
    }
}
