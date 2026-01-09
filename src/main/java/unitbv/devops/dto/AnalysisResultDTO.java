package unitbv.devops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pentru rezultatele de analiză primite de la serviciul C++
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultDTO {
    private String symbolCode;
    private BigDecimal currentPrice;
    private BigDecimal sma;        // Simple Moving Average
    private BigDecimal ema;        // Exponential Moving Average
    private Long volume;
    private LocalDateTime timestamp;
    private Integer windowSize;    // Window size pentru moving average (ex: 5, 15, 60 minute)
}

