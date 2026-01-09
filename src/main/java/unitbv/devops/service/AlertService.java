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
     * Obține toate alertele cu filtre opționale
     */
    public List<AlertDTO> getAllAlerts(String symbolCode, String alertType, LocalDateTime startDate, LocalDateTime endDate) {
        List<Alert> alerts;

        if (symbolCode != null && alertType != null) {
            alerts = alertRepository.findBySymbol_SymbolCodeAndAlertTypeOrderByTriggeredAtDesc(symbolCode, alertType);
        } else if (symbolCode != null) {
            alerts = alertRepository.findBySymbol_SymbolCodeOrderByTriggeredAtDesc(symbolCode);
        } else if (alertType != null) {
            alerts = alertRepository.findByAlertTypeOrderByTriggeredAtDesc(alertType);
        } else {
            alerts = alertRepository.findAll();
        }

        // Filtrare după date dacă sunt specificate
        if (startDate != null && endDate != null) {
            alerts = alerts.stream()
                    .filter(alert -> !alert.getTriggeredAt().isBefore(startDate) && !alert.getTriggeredAt().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        return alerts.stream()
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
     * Obține alertele active (neconfirmate)
     */
    public List<AlertDTO> getActiveAlerts() {
        return alertRepository.findByAcknowledgedOrderByTriggeredAtDesc(false)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Marchează o alertă ca fiind confirmată
     */
    public Optional<AlertDTO> acknowledgeAlert(Long id) {
        return alertRepository.findById(id)
                .map(alert -> {
                    alert.setAcknowledged(true);
                    Alert updated = alertRepository.save(alert);
                    return convertToDTO(updated);
                });
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
                alertDTO.getTriggeredAt() != null ? alertDTO.getTriggeredAt() : LocalDateTime.now(),
                alertDTO.getDetails()
        );
        if (alertDTO.getAcknowledged() != null) {
            alert.setAcknowledged(alertDTO.getAcknowledged());
        }
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
                    if (alertDTO.getAcknowledged() != null) {
                        alert.setAcknowledged(alertDTO.getAcknowledged());
                    }
                    Alert updated = alertRepository.save(alert);
                    return convertToDTO(updated);
                });
    }

    /**
     * Șterge o alertă
     */
    public void deleteAlert(Long id) {
        alertRepository.deleteById(id);
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
                alert.getDetails(),
                alert.getAcknowledged()
        );
    }
}

