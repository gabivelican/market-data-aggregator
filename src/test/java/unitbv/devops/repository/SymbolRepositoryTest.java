package unitbv.devops.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import unitbv.devops.DatabaseTestBase;
import unitbv.devops.entity.Symbol;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SymbolRepositoryTest extends DatabaseTestBase {

    @Autowired
    private SymbolRepository symbolRepository;

    @Test
    public void testFindSymbolByCode_AAPL() {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode("AAPL");
        assertTrue(symbol.isPresent(), "AAPL symbol should exist in seed data");
        assertEquals("Apple Inc.", symbol.get().getName());
        assertEquals("STOCK", symbol.get().getType());
    }

    @Test
    public void testFindSymbolByCode_BTC() {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode("BTC");
        assertTrue(symbol.isPresent(), "BTC symbol should exist in seed data");
        assertEquals("Bitcoin", symbol.get().getName());
        assertEquals("CRYPTOCURRENCY", symbol.get().getType());
    }

    @Test
    public void testFindSymbolByCode_GOOGL() {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode("GOOGL");
        assertTrue(symbol.isPresent(), "GOOGL symbol should exist in seed data");
        assertEquals("Alphabet Inc.", symbol.get().getName());
        assertEquals("STOCK", symbol.get().getType());
    }

    @Test
    public void testFindAllSymbols() {
        Iterable<Symbol> symbols = symbolRepository.findAll();
        int count = 0;
        for (Symbol symbol : symbols) {
            count++;
        }
        assertTrue(count >= 3, "Should have at least 3 symbols");
    }

    @Test
    public void testSymbolNotFound() {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode("NONEXISTENT");
        assertFalse(symbol.isPresent(), "Non-existent symbol should not be found");
    }

    @Test
    public void testCreateNewSymbol() {
        Symbol newSymbol = new Symbol("NVDA", "NVIDIA Corporation", "STOCK");
        Symbol saved = symbolRepository.save(newSymbol);
        assertNotNull(saved.getId(), "Saved symbol should have an ID");

        Optional<Symbol> found = symbolRepository.findBySymbolCode("NVDA");
        assertTrue(found.isPresent(), "Newly created symbol should be findable");
        assertEquals("NVIDIA Corporation", found.get().getName());
    }
}
