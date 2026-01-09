package unitbv.devops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pentru reprezentarea alertelor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    private Long id;
    private String symbolCode;
    private String alertType;
    private BigDecimal threshold;
    private LocalDateTime triggeredAt;
    private String details;
    private Boolean acknowledged;
}

