package unitbv.devops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import unitbv.devops.dto.PriceDTO;
import unitbv.devops.dto.AlertDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pentru broadcasting de mesaje în timp real prin WebSocket
 */
@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast price update către toți subscriberii pe /topic/prices
     */
    public void broadcastPriceUpdate(PriceDTO priceDTO) {
        logger.debug("Broadcasting price update for symbol: {} - Price: {}",
                    priceDTO.getSymbolCode(), priceDTO.getPrice());

        // Broadcast către toți subscriberii pe /topic/prices
        messagingTemplate.convertAndSend("/topic/prices", priceDTO);

        // Broadcast către subscriberii specifici pe /topic/prices/{symbol}
        messagingTemplate.convertAndSend("/topic/prices/" + priceDTO.getSymbolCode(), priceDTO);
    }

    /**
     * Broadcast alert către toți subscriberii pe /topic/alerts
     */
    public void broadcastAlert(AlertDTO alertDTO) {
        logger.info("Broadcasting alert for symbol: {} - Type: {}",
                   alertDTO.getSymbolCode(), alertDTO.getAlertType());

        // Broadcast către toți subscriberii pe /topic/alerts
        messagingTemplate.convertAndSend("/topic/alerts", alertDTO);

        // Broadcast către subscriberii specifici pe /topic/alerts/{symbol}
        messagingTemplate.convertAndSend("/topic/alerts/" + alertDTO.getSymbolCode(), alertDTO);
    }

    /**
     * Broadcast price updates pentru un simbol specific
     */
    public void broadcastSymbolPrice(String symbolCode, PriceDTO priceDTO) {
        logger.debug("Broadcasting price for specific symbol: {}", symbolCode);
        messagingTemplate.convertAndSend("/topic/prices/" + symbolCode, priceDTO);
    }

    /**
     * Broadcast alert pentru un simbol specific
     */
    public void broadcastSymbolAlert(String symbolCode, AlertDTO alertDTO) {
        logger.info("Broadcasting alert for specific symbol: {}", symbolCode);
        messagingTemplate.convertAndSend("/topic/alerts/" + symbolCode, alertDTO);
    }
}

