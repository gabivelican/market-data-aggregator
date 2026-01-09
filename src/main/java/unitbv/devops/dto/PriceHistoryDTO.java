package unitbv.devops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO pentru reprezentarea istoricului de prețuri cu statistici
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryDTO {
    private String symbolCode;
    private List<PriceDTO> prices;
    private PriceStatistics statistics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceStatistics {
        private BigDecimal average;
        private BigDecimal min;
        private BigDecimal max;
        private Long totalVolume;
        private Integer count;
    }
}

