import requests
import os
import logging
import pgquery_rsu

class RsuStatusFetch:
    def __init__(self):
        self.ZABBIX_ENDPOINT = os.environ['ZABBIX_ENDPOINT']
        self.ZABBIX_AUTH = ''

    def setZabbixAuth(self):
        logging.info(f'Fetching Zabbix auth token from {self.ZABBIX_ENDPOINT}')
        zabbixAuthPayload = {
            "jsonrpc": "2.0",
            "method": "user.login",
            "id": 1,
            "params": {
                "username": os.environ['ZABBIX_USER'],
                "password": os.environ['ZABBIX_PASSWORD']
            }
        }

        zabbixAuthResponse = requests.post(self.ZABBIX_ENDPOINT, json=zabbixAuthPayload)
        self.ZABBIX_AUTH = zabbixAuthResponse.json()['result']

    def getHostInfo(self, rsu_ip):
        hostPayload = {
            "jsonrpc": "2.0",
            "method": "host.get",
            "id": 1,
            "auth": self.ZABBIX_AUTH,
            "params": {
                "output": [
                    "hostid",
                    "host"
                ],
                "selectInterfaces": [
                    "interfaceid",
                    "ip"
                ],
                "filter": {
                    "ip": rsu_ip
                }
            }
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
                "hostids": [hostInfo['result'][0]['hostid']],
                "filter": {"key_": "icmpping"}
            }
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
                "itemids": [zabbix_item['result'][0]['itemid']],
                "output": "extend",
                "sortfield": "clock",
                "sortorder": "DESC",
                "limit": 5
            }
        }
        getHistoryResponse = requests.post(self.ZABBIX_ENDPOINT, json=historyPayload)
        return getHistoryResponse.json()

    def insertHistoryItem(self, zabbix_history, rsu_item):
        historyItemPayload = {
            "histories": zabbix_history['result'],
            "rsu_id": rsu_item['rsu_id']
        }
        logging.info(f'Inserting {len(zabbix_history["result"])} history items for RSU {rsu_item["rsu_ip"]}')
        return pgquery_rsu.insert_rsu_ping(historyItemPayload)

    def printConfigInfo(self):
        configObject = {
            'ZABBIX_ENDPOINT' : self.ZABBIX_ENDPOINT,
            'ZABBIX_AUTH' : self.ZABBIX_AUTH
        }
        logging.info(f'Configuration: {configObject}')

    def run(self):
        self.setZabbixAuth()
        self.printConfigInfo()
        rsu_items = pgquery_rsu.get_rsu_data()
        logging.info(f'Found {len(rsu_items)} RSUs to fetch status for')

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
                    logging.warning(f'Failed to insert history item for {rsu_item["rsu_ip"]}')
            except Exception as e:
                logging.error(f'Failed to fetch Zabbix data RSU {rsu_item["rsu_ip"]}')
        return

if __name__ == "__main__":
    # Configure logging based on ENV var or use default if not set
    log_level = 'INFO' if "LOGGING_LEVEL" not in os.environ else os.environ['LOGGING_LEVEL']
    logging.basicConfig(format='%(levelname)s:%(message)s', level=log_level)

    rsf = RsuStatusFetch()
    rsf.run()
