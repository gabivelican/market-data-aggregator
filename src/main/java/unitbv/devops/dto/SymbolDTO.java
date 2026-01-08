package unitbv.devops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pentru reprezentarea unui simbol de tranzacționare
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymbolDTO {
    private Long id;
    private String symbolCode;
    private String name;
    private String type;
}

