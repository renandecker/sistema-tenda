package br.com.sistematenda.common.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "scale")
public interface ScaleConfig {

    @WithDefault("COM1")
    String port();

    @WithName("baud-rate")
    @WithDefault("9600")
    int baudRate();

    @WithName("data-bits")
    @WithDefault("8")
    int dataBits();

    @WithName("stop-bits")
    @WithDefault("1")
    int stopBits();

    @WithDefault("NONE")
    String parity();

    @WithName("timeout-ms")
    @WithDefault("5000")
    int timeoutMs();

    @WithDefault("TOLEDO")
    String protocol();

    @WithName("auto-connect")
    @WithDefault("true")
    boolean autoConnect();

    @WithName("reconnect-interval-ms")
    @WithDefault("10000")
    int reconnectIntervalMs();
}
