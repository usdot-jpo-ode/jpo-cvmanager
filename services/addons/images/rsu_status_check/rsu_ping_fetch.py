import requests
import os
import logging
import common.pgquery as pgquery


def get_rsu_data():
    result = []

    # Execute the query and fetch all results
    query = "SELECT rsu_id, ipv4_address FROM public.rsus ORDER BY rsu_id"
    data = pgquery.query_db(query)

    logging.debug("Parsing results...")
    for point in data:
        rsu = {"rsu_id": point[0], "rsu_ip": str(point[1])}
        result.append(rsu)

    return result


def insert_rsu_ping(request_json):
    rsu_id = request_json["rsu_id"]
    histories = request_json["histories"]

    logging.debug(f"Inserting {len(histories)} new Ping records for RsuData {rsu_id}")
    for history in histories:
        try:
            query = f'INSERT INTO public.ping (timestamp, result, rsu_id) VALUES (to_timestamp({history["clock"]}), B\'{history["value"]}\', {rsu_id})'
            pgquery.write_db(query)
        except Exception as e:
            logging.exception(f"Error inserting Ping record: {e}")
            return False

    return True


class RsuStatusFetch:
    def __init__(self):
        self.ZABBIX_ENDPOINT = os.environ["ZABBIX_ENDPOINT"]
        self.ZABBIX_AUTH = ""

    def setZabbixAuth(self):
        logging.info(f"Fetching Zabbix auth token from {self.ZABBIX_ENDPOINT}")
        zabbixAuthPayload = {
            "jsonrpc": "2.0",
            "method": "user.login",
            "id": 1,
            "params": {
                "username": os.environ["ZABBIX_USER"],
                "password": os.environ["ZABBIX_PASSWORD"],
            },
        }

        zabbixAuthResponse = requests.post(self.ZABBIX_ENDPOINT, json=zabbixAuthPayload)
        self.ZABBIX_AUTH = zabbixAuthResponse.json()["result"]

    def getHostInfo(self, rsu_ip):
        hostPayload = {
            "jsonrpc": "2.0",
            "method": "host.get",
            "id": 1,
            "auth": self.ZABBIX_AUTH,
            "params": {
                "output": ["hostid", "host"],
                "selectInterfaces": ["interfaceid", "ip"],
                "filter": {"ip": rsu_ip},
            },
        }
        hostInfoResponse = requests.post(self.ZABBIX_ENDPOINT, json=hostPayload)
        return hostInfoResponse.json()

    def getItem(self, hostInfo):
        itemPayload = {
            "jsonrpc": "2.0",
            "method": "item.get",
            "id": 1,
            "auth": self.ZABBIX_AUTH,
            "params": {
                "hostids": [hostInfo["result"][0]["hostid"]],
                "filter": {"key_": "icmpping"},
            },
        }
        getItemResponse = requests.post(self.ZABBIX_ENDPOINT, json=itemPayload)
        return getItemResponse.json()

    def getHistory(self, zabbix_item):
        historyPayload = {
            "jsonrpc": "2.0",
            "method": "history.get",
            "id": 1,
            "auth": self.ZABBIX_AUTH,
            "params": {
                "itemids": [zabbix_item["result"][0]["itemid"]],
                "output": "extend",
                "sortfield": "clock",
                "sortorder": "DESC",
                "limit": 5,
            },
        }
        getHistoryResponse = requests.post(self.ZABBIX_ENDPOINT, json=historyPayload)
        return getHistoryResponse.json()

    def insertHistoryItem(self, zabbix_history, rsu_item):
        historyItemPayload = {
            "histories": zabbix_history["result"],
            "rsu_id": rsu_item["rsu_id"],
        }
        logging.info(
            f'Inserting {len(zabbix_history["result"])} history items for RSU {rsu_item["rsu_ip"]}'
        )
        return insert_rsu_ping(historyItemPayload)

    def printConfigInfo(self):
        configObject = {
            "ZABBIX_ENDPOINT": self.ZABBIX_ENDPOINT,
            "ZABBIX_AUTH": self.ZABBIX_AUTH,
        }
        logging.info(f"Configuration: {configObject}")

    def run(self):
        self.setZabbixAuth()
        self.printConfigInfo()
        rsu_items = get_rsu_data()
        logging.info(f"Found {len(rsu_items)} RSUs to fetch status for")

        # loop over rsuInfo, get host info
        for rsu_item in rsu_items:
            try:
                hostInfo = self.getHostInfo(rsu_item["rsu_ip"])
                # with host info, get items
                zabbix_item = self.getItem(hostInfo)
                # with item get history
                zabbix_history = self.getHistory(zabbix_item)
                # with history, insert history item
                insertSuccess = self.insertHistoryItem(zabbix_history, rsu_item)
                if not insertSuccess:
                    logging.warning(
                        f'Failed to insert history item for {rsu_item["rsu_ip"]}'
                    )
            except Exception as e:
                logging.error(f'Failed to fetch Zabbix data RSU {rsu_item["rsu_ip"]}')
        return


if __name__ == "__main__":
    # Configure logging based on ENV var or use default if not set
    log_level = os.environ.get("LOGGING_LEVEL", "INFO")
    log_level = "INFO" if log_level == "" else log_level
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    run_service = (
        os.environ.get("RSU_PING", "False").lower() == "true"
        and os.environ.get("ZABBIX", "False").lower() == "true"
    )
    if not run_service:
        logging.info("The rsu-ping-fetch service is disabled and will not run")
        exit()

    rsf = RsuStatusFetch()
    rsf.run()
