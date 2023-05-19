import requests
import os
import logging
import pgquery_rsu

class RsuStatusFetch:
    def __init__(self):
        self.CF_TRIGGER_BASE = os.environ['CF_TRIGGER_BASE_URL']
        self.RSU_INFO_ENDPOINT = self.CF_TRIGGER_BASE + '/rsu-info'
        self.STATUS_HISTORY_INSERT_ENDPOINT = self.CF_TRIGGER_BASE + '/rsu-ping-insert'

        self.ZABBIX_ENDPOINT = os.environ['ZABBIX_ENDPOINT']
        self.ZABBIX_AUTH = ''

    def setZabbixAuth(self):
        logging.info(f'Fetching Zabbix auth token from {self.ZABBIX_ENDPOINT}')
        zabbixAuthPayload = {
            "jsonrpc": "2.0",
            "method": "user.login",
            "id": 1,
            "params": {
                "user": os.environ['ZABBIX_USER'],
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

    def insertHistoryItem(self, zabbix_history, rsu_ip):
        token = self.gen_auth(self.STATUS_HISTORY_INSERT_ENDPOINT)
        h = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {token}"
        }
        historyItemPayload = {
            "histories": zabbix_history['result'],
            "rsuData": rsu_ip
        }
        logging.info(f'Inserting {len(zabbix_history["result"])} history items for RSU {rsu_ip}')
        historyItemResponse = requests.post(self.STATUS_HISTORY_INSERT_ENDPOINT, headers=h, json=historyItemPayload)
        # logging.debug(historyItemResponse.json())        
        return historyItemResponse.status_code == 200

    def printConfigInfo(self):
        configObject = {
            'CF_TRIGGER_BASE': self.CF_TRIGGER_BASE,
            'ZABBIX_ENDPOINT' : self.ZABBIX_ENDPOINT,
            'ZABBIX_AUTH' : self.ZABBIX_AUTH
        }
        logging.info(f'Configuration: {configObject}')

    def run(self):
        self.setZabbixAuth()
        self.printConfigInfo()
        rsu_ips = pgquery_rsu.get_rsu_ips()
        logging.info(f'Found {len(rsu_ips)} RSUs to fetch status for')

        # loop over rsuInfo, get host info
        for rsu_ip in rsu_ips:
            try:
                hostInfo = self.getHostInfo(rsu_ip)
                # with host info, get items
                zabbix_item = self.getItem(hostInfo)
                # with item get history
                zabbix_history = self.getHistory(zabbix_item)
                # with history, insert history item
                insertSuccess = self.insertHistoryItem(zabbix_history, rsu_ip)
                if not insertSuccess:
                    logging.warning(f'Failed to insert history item for {rsu_ip}')
            except Exception as e:
                logging.error(f'Failed to fetch Zabbix data RSU {rsu_ip}')
        return

if __name__ == "__main__":
    # Configure logging based on ENV var or use default if not set
    log_level = 'INFO' if "LOGGING_LEVEL" not in os.environ else os.environ['LOGGING_LEVEL']
    logging.basicConfig(format='%(levelname)s:%(message)s', level=log_level)

    rsf = RsuStatusFetch()
    rsf.run()