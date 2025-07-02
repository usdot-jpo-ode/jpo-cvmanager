from abc import ABC, abstractmethod
import common.pgquery as pgquery


# An interface for custom scripts that update SNMP configurations in the PostgreSQL database
class UpdatePostgresSnmpAbstractClass(ABC):
    def get_rsu_list(self):
        query = (
            "SELECT to_jsonb(row) "
            "FROM ("
            "SELECT rd.rsu_id, rd.ipv4_address, snmp.username AS snmp_username, snmp.password AS snmp_password, snmp.encrypt_password AS snmp_encrypt_pw, sver.protocol_code AS snmp_version "
            "FROM public.rsus AS rd "
            "LEFT JOIN public.snmp_credentials AS snmp ON snmp.snmp_credential_id = rd.snmp_credential_id "
            "LEFT JOIN public.snmp_protocols AS sver ON sver.snmp_protocol_id = rd.snmp_protocol_id"
            ") as row"
        )

        # Query PostgreSQL for the list of RSU IPs with SNMP credentials and version
        data = pgquery.query_db(query)

        rsu_list = []
        for row in data:
            rsu_list.append(dict(row[0]))

        return rsu_list

    def insert_config_list(self, snmp_config_list):
        """Insert a list of SNMP configurations into the PostgreSQL database."""
        return None

    def delete_config_list(self, snmp_config_list):
        """Delete a list of SNMP configurations from the PostgreSQL database."""
        return None

    def get_config_list(self, rsu_obj={}):
        """Retrieve a list of SNMP configurations from the PostgreSQL database."""
        return None

    @abstractmethod
    def update_postgresql(self, rsu_snmp_configs_obj, subset=False):
        """Update PostgreSQL with the latest SNMP configurations."""
        pass

    @abstractmethod
    def get_snmp_configs(self, rsu_list):
        """Retrieve SNMP configurations for a list of RSUs."""
        pass
