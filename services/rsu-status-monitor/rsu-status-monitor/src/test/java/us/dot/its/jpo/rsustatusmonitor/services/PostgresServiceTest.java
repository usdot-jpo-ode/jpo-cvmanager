package us.dot.its.jpo.rsustatusmonitor.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import us.dot.its.jpo.rsustatusmonitor.models.postgres.derived.RsuData;
import us.dot.its.jpo.rsustatusmonitor.models.postgres.derived.RsuSnmpCredentials;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostgresServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<RsuSnmpCredentials> rsuCredentialsQuery;

    @Mock
    private TypedQuery<RsuData> rsuDataQuery;

    @InjectMocks
    private PostgresService service;

    @Test
    public void testGetRsusWithCredentials_AllRsus_Success() {
        RsuSnmpCredentials cred1 = new RsuSnmpCredentials(1, "192.168.1.1", "user1", "pass1", "encPass1", "SNMPv3",
                "12345");
        RsuSnmpCredentials cred2 = new RsuSnmpCredentials(2, "192.168.1.2", "user2", "pass2", "encPass2", "SNMPv2c",
                "67890");
        List<RsuSnmpCredentials> expectedCredentials = Arrays.asList(cred1, cred2);

        when(entityManager.createQuery(anyString(), eq(RsuSnmpCredentials.class))).thenReturn(rsuCredentialsQuery);
        when(rsuCredentialsQuery.getResultList()).thenReturn(expectedCredentials);

        List<RsuSnmpCredentials> result = service.getRsusWithCredentials();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getRsu_id());
        assertEquals("192.168.1.1", result.get(0).getIpv4_address());
        assertEquals("12345", result.get(0).getIntersection_id());
        verify(entityManager).createQuery(anyString(), eq(RsuSnmpCredentials.class));
        verify(rsuCredentialsQuery).getResultList();
    }

    @Test
    public void testGetRsusWithCredentials_AllRsus_EmptyResult() {
        when(entityManager.createQuery(anyString(), eq(RsuSnmpCredentials.class))).thenReturn(rsuCredentialsQuery);
        when(rsuCredentialsQuery.getResultList()).thenReturn(Collections.emptyList());

        List<RsuSnmpCredentials> result = service.getRsusWithCredentials();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(entityManager).createQuery(anyString(), eq(RsuSnmpCredentials.class));
        verify(rsuCredentialsQuery).getResultList();
    }

    @Test
    public void testGetRsusWithCredentials_AllRsus_WithNullIntersectionId() {
        RsuSnmpCredentials credWithNullIntersection = new RsuSnmpCredentials(3, "192.168.1.3", "user3", "pass3",
                "encPass3", "SNMPv3", null);
        List<RsuSnmpCredentials> expectedCredentials = Collections.singletonList(credWithNullIntersection);

        when(entityManager.createQuery(anyString(), eq(RsuSnmpCredentials.class))).thenReturn(rsuCredentialsQuery);
        when(rsuCredentialsQuery.getResultList()).thenReturn(expectedCredentials);

        List<RsuSnmpCredentials> result = service.getRsusWithCredentials();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getRsu_id());
        assertNull(result.get(0).getIntersection_id());
        verify(entityManager).createQuery(anyString(), eq(RsuSnmpCredentials.class));
        verify(rsuCredentialsQuery).getResultList();
    }

    @Test
    public void testGetRsusWithCredentials_QueryExecutionException() {
        when(entityManager.createQuery(anyString(), eq(RsuSnmpCredentials.class))).thenReturn(rsuCredentialsQuery);
        when(rsuCredentialsQuery.getResultList()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> service.getRsusWithCredentials());
        verify(entityManager).createQuery(anyString(), eq(RsuSnmpCredentials.class));
        verify(rsuCredentialsQuery).getResultList();
    }
}
