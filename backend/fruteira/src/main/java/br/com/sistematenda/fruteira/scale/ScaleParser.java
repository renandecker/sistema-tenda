package br.com.sistematenda.fruteira.scale;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class ScaleParser {

    private static final Logger LOG = Logger.getLogger(ScaleParser.class);

    // Padrões para diferentes protocolos de balança
    private static final Pattern TOLEDO_PATTERN = Pattern.compile(".*?([+-]?\\d+\\.?\\d*)\\s*(kg|g|lb|oz)?.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern FILIZOLA_PATTERN = Pattern.compile("^\\s*ST\\s+([+-]?\\d+\\.?\\d*)\\s*(kg|g)?.*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern URANO_PATTERN = Pattern.compile("^\\s*(\\d+\\.?\\d*)\\s*(kg|g)?.*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GENERIC_PATTERN = Pattern.compile("([+-]?\\d+\\.?\\d*)");

    public ScaleReading parse(String rawData, String protocol) {
        if (rawData == null || rawData.trim().isEmpty()) {
            return new ScaleReading(null, false, "Dados vazios");
        }

        String cleanData = rawData.trim();
        LOG.debugf("Parsing scale data: %s (protocol: %s)", cleanData, protocol);

        try {
            BigDecimal weight = null;
            String unit = "kg";
            boolean stable = false;

            switch (protocol.toUpperCase()) {
                case "TOLEDO" -> {
                    Matcher m = TOLEDO_PATTERN.matcher(cleanData);
                    if (m.matches()) {
                        weight = new BigDecimal(m.group(1));
                        if (m.group(2) != null) unit = m.group(2).toLowerCase();
                        stable = cleanData.contains("ST") || cleanData.contains("st");
                    }
                }
                case "FILIZOLA" -> {
                    Matcher m = FILIZOLA_PATTERN.matcher(cleanData);
                    if (m.matches()) {
                        weight = new BigDecimal(m.group(1));
                        if (m.group(2) != null) unit = m.group(2).toLowerCase();
                        stable = true; // Filizola ST indicates stable
                    }
                }
                case "URANO" -> {
                    Matcher m = URANO_PATTERN.matcher(cleanData);
                    if (m.matches()) {
                        weight = new BigDecimal(m.group(1));
                        if (m.group(2) != null) unit = m.group(2).toLowerCase();
                        stable = true;
                    }
                }
                default -> {
                    Matcher m = GENERIC_PATTERN.matcher(cleanData);
                    if (m.find()) {
                        weight = new BigDecimal(m.group(1));
                    }
                }
            }

            if (weight == null) {
                return new ScaleReading(null, false, "Não foi possível extrair peso: " + cleanData);
            }

            // Convert to kg if needed
            if ("g".equals(unit)) {
                weight = weight.divide(new BigDecimal("1000"));
            } else if ("lb".equals(unit)) {
                weight = weight.multiply(new BigDecimal("0.453592"));
            } else if ("oz".equals(unit)) {
                weight = weight.multiply(new BigDecimal("0.0283495"));
            }

            return new ScaleReading(weight, stable, null);

        } catch (Exception e) {
            LOG.error("Erro ao parsear dados da balança", e);
            return new ScaleReading(null, false, "Erro de parse: " + e.getMessage());
        }
    }

    public static class ScaleReading {
        private final BigDecimal weightKg;
        private final boolean stable;
        private final String error;

        public ScaleReading(BigDecimal weightKg, boolean stable, String error) {
            this.weightKg = weightKg;
            this.stable = stable;
            this.error = error;
        }

        public BigDecimal getWeightKg() {
            return weightKg;
        }

        public boolean isStable() {
            return stable;
        }

        public String getError() {
            return error;
        }

        public boolean isValid() {
            return error == null && weightKg != null && weightKg.compareTo(BigDecimal.ZERO) >= 0;
        }
    }
}