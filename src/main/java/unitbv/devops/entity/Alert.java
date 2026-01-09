package unitbv.devops.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alerts_symbol_id", columnList = "symbol_id"),
    @Index(name = "idx_alerts_triggered_at", columnList = "triggered_at")
})
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol_id", nullable = false)
    private Symbol symbol;

    @Column(nullable = false, length = 50)
    private String alertType;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal threshold;

    @Column(nullable = false)
    private LocalDateTime triggeredAt;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private Boolean acknowledged = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public Alert() {}

    public Alert(Symbol symbol, String alertType, BigDecimal threshold, LocalDateTime triggeredAt, String details) {
        this.symbol = symbol;
        this.alertType = alertType;
        this.threshold = threshold;
        this.triggeredAt = triggeredAt;
        this.details = details;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Boolean getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
