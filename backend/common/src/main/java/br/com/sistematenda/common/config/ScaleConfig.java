package br.com.sistematenda.common.config;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "scale")
public class ScaleConfig {

    @ConfigItem(name = "port", defaultValue = "COM1")
    public String port;

    @ConfigItem(name = "baud-rate", defaultValue = "9600")
    public int baudRate;

    @ConfigItem(name = "data-bits", defaultValue = "8")
    public int dataBits;

    @ConfigItem(name = "stop-bits", defaultValue = "1")
    public int stopBits;

    @ConfigItem(name = "parity", defaultValue = "NONE")
    public String parity;

    @ConfigItem(name = "timeout-ms", defaultValue = "5000")
    public int timeoutMs;

    @ConfigItem(name = "protocol", defaultValue = "TOLEDO")
    public String protocol;

    @ConfigItem(name = "auto-connect", defaultValue = "true")
    public boolean autoConnect;

    @ConfigItem(name = "reconnect-interval-ms", defaultValue = "10000")
    public int reconnectIntervalMs;
}