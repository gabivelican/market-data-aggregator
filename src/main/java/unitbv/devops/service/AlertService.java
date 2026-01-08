package unitbv.devops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unitbv.devops.entity.Alert;
import unitbv.devops.entity.Symbol;
import unitbv.devops.repository.AlertRepository;
import unitbv.devops.repository.SymbolRepository;
import unitbv.devops.dto.AlertDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer pentru operații pe alerte
 */
@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private SymbolRepository symbolRepository;

    /**
     * Obține toate alertele
     */
    public List<AlertDTO> getAllAlerts() {
        return alertRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obține alertele pentru un simbol
     */
    public List<AlertDTO> getAlertsBySymbol(String symbolCode) {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode(symbolCode);
        if (symbol.isEmpty()) {
            return List.of();
        }

        return alertRepository.findBySymbolOrderByTriggeredAtDesc(symbol.get())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obține alertele în interval de date
     */
    public List<AlertDTO> getAlertsByDateRange(String symbolCode, LocalDateTime startDate, LocalDateTime endDate) {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode(symbolCode);
        if (symbol.isEmpty()) {
            return List.of();
        }

        return alertRepository.findBySymbolAndTriggeredAtBetween(symbol.get(), startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obține o alertă specifică după ID
     */
    public Optional<AlertDTO> getAlertById(Long id) {
        return alertRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Creează o alertă nouă
     */
    public AlertDTO createAlert(AlertDTO alertDTO) {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode(alertDTO.getSymbolCode());
        if (symbol.isEmpty()) {
            throw new IllegalArgumentException("Symbol not found: " + alertDTO.getSymbolCode());
        }

        Alert alert = new Alert(
                symbol.get(),
                alertDTO.getAlertType(),
                alertDTO.getThreshold(),
                alertDTO.getTriggeredAt(),
                alertDTO.getDetails()
        );
        Alert saved = alertRepository.save(alert);
        return convertToDTO(saved);
    }

    /**
     * Actualizează o alertă existentă
     */
    public Optional<AlertDTO> updateAlert(Long id, AlertDTO alertDTO) {
        return alertRepository.findById(id)
                .map(alert -> {
                    Optional<Symbol> symbol = symbolRepository.findBySymbolCode(alertDTO.getSymbolCode());
                    if (symbol.isPresent()) {
                        alert.setSymbol(symbol.get());
                    }
                    alert.setAlertType(alertDTO.getAlertType());
                    alert.setThreshold(alertDTO.getThreshold());
                    alert.setTriggeredAt(alertDTO.getTriggeredAt());
                    alert.setDetails(alertDTO.getDetails());
                    Alert updated = alertRepository.save(alert);
                    return convertToDTO(updated);
                });
    }

    /**
     * Șterge o alertă
     */
    public boolean deleteAlert(Long id) {
        if (alertRepository.existsById(id)) {
            alertRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Convertor de Entity la DTO
     */
    private AlertDTO convertToDTO(Alert alert) {
        return new AlertDTO(
                alert.getId(),
                alert.getSymbol().getSymbolCode(),
                alert.getAlertType(),
                alert.getThreshold(),
                alert.getTriggeredAt(),
                alert.getDetails()
        );
    }
}
