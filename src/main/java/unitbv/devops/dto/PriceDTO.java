package unitbv.devops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pentru reprezentarea datelor de preț
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceDTO {
    private Long id;
    private String symbolCode;
    private BigDecimal price;
    private Long volume;
    private LocalDateTime timestamp;
}

