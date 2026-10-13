package br.com.sistematenda.common.config;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "app")
public class AppConfig {

    @ConfigItem(name = "name", defaultValue = "Sistema Tenda")
    public String name;

    @ConfigItem(name = "version", defaultValue = "1.0.0")
    public String version;

    @ConfigItem(name = "timezone", defaultValue = "America/Sao_Paulo")
    public String timezone;

    @ConfigItem(name = "currency", defaultValue = "BRL")
    public String currency;

    @ConfigItem(name = "locale", defaultValue = "pt_BR")
    public String locale;
}