package us.dot.its.jpo.ode.api.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * MapStruct mapper for converting between InetAddress and String.
 * This mapper is automatically used by other mappers when they need to convert
 * IP addresses.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface INetMapper {

    /**
     * Converts InetAddress to String representation (e.g., "192.168.1.1")
     */
    default String mapInetAddressToString(InetAddress inetAddress) {
        if (inetAddress == null) {
            return null;
        }
        return inetAddress.getHostAddress();
    }

    /**
     * Converts String IP address to InetAddress object
     * 
     * @throws IllegalArgumentException if the IP address is invalid
     */
    default InetAddress mapStringToInetAddress(String ipAddress) {
        if (ipAddress == null) {
            return null;
        }
        try {
            return InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP address: " + ipAddress, e);
        }
    }
}