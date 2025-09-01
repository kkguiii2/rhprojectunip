package com.shiftly;

import com.shiftly.app.ShiftlyApplication;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste básico para verificar funcionamento da aplicação
 */
public class ShiftlyApplicationTest {
    
    @Test
    public void testApplicationInfo() {
        String info = ShiftlyApplication.getApplicationInfo();
        assertNotNull(info);
        assertTrue(info.contains("Shiftly"));
        assertTrue(info.contains("1.0.0"));
    }
    
    @Test
    public void testApplicationVersion() {
        String version = ShiftlyApplication.getVersion();
        assertNotNull(version);
        assertEquals("1.0.0", version);
    }
}
