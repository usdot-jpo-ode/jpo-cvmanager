import logging
import datetime
import requests
from common.keycloak_api import KeycloakServiceAccountApi


class EmailApi:
    def __init__(self, iapi_base_url, kc_api: KeycloakServiceAccountApi):
        """
        Initialize the EmailApi with the base URL and Keycloak service account API.

        Args:
            iapi_base_url (str): The base URL for the email API.
            kc_api (KeycloakServiceAccountApi): The Keycloak service account API
                used to obtain authentication tokens for email API requests.
        """
        self.iapi_endpoint = iapi_base_url
        self.kc_api = kc_api

    def _build_response(self, response: requests.Response) -> tuple[int, dict]:
        if not (200 <= response.status_code < 300):
            logging.error(
                f"Email API request failed: {response.status_code} - {response.text}"
            )
        try:
            response_data = response.json()
            if isinstance(response_data, dict):
                return response.status_code, response_data
            return response.status_code, {"data": response_data}
        except ValueError:
            return response.status_code, {"error": response.text}

    def _handle_request_exception(
        self, exc: requests.RequestException
    ) -> tuple[int, dict]:
        logging.exception("Email API request failed: %s", exc)
        return 500, {"error": str(exc)}

    def send_message_counts(
        self,
        org_name: str,
        deployment_title: str,
        start_date: datetime.datetime,
        end_date: datetime.datetime,
        message_type_list: list[str],
        rsu_counts: list[dict],
    ) -> tuple[int, dict]:
        """
        Send a message counts email via the API.

        Args:
            org_name (str): Organization name.
            deployment_title (str): Deployment title.
            start_date (datetime.datetime): Start date.
            end_date (datetime.datetime): End date.
            message_type_list (list[str]): List of message types.
            rsu_counts (list[dict]): List of count dictionaries.

        Returns:
            tuple[int, dict]: The HTTP status code and the response JSON.
        """
        token = self.kc_api.get_kc_token()
        if not token:
            return 500, {"error": "Unable to obtain Keycloak token."}
        try:
            response = requests.post(
                f"{self.iapi_endpoint}/emails/message-counts",
                headers={"Authorization": f"Bearer {token['access_token']}"},
                json={
                    "org_name": org_name,
                    "deployment_title": deployment_title,
                    "start_date": start_date.timestamp(),
                    "end_date": end_date.timestamp(),
                    "message_type_list": message_type_list,
                    "rsu_counts": rsu_counts,
                },
                timeout=10,
            )
        except requests.RequestException as exc:
            return self._handle_request_exception(exc)
        return self._build_response(response)

    def send_firmware_upgrade_failure(
        self, rsu_ip: str, error_message: str, failure_type: str, stack_trace: str
    ) -> tuple[int, dict]:
        """
        Send a firmware upgrade failure email via the API.

        Args:
            rsu_ip (str): RSU IP address.
            error_message (str): Error message.
            failure_type (str): Type of failure.
            stack_trace (str): Stack trace.

        Returns:
            tuple[int, str]: The HTTP status code and the response JSON.
        """
        token = self.kc_api.get_kc_token()
        if not token:
            return 500, {"error": "Unable to obtain Keycloak token."}

        try:
            response = requests.post(
                f"{self.iapi_endpoint}/emails/firmware-upgrade-failures",
                headers={"Authorization": f"Bearer {token['access_token']}"},
                json={
                    "rsu_ip": rsu_ip,
                    "message": error_message,
                    "failure_type": failure_type,
                    "stack_trace": stack_trace,
                },
                timeout=10,
            )
        except requests.RequestException as exc:
            return self._handle_request_exception(exc)
        return self._build_response(response)

    def send_api_error_email(
        self,
        error_message: str,
        stack_trace: str,
        timestamp: str,
        logs_link: str,
    ) -> tuple[int, dict]:
        """
        Send a critical api error email via the API.

        Args:
            error_message (str): Error message.
            stack_trace (str): Stack trace.
            timestamp (str): Timestamp of the error in ISO format.
            logs_link (str): Link to the logs.

        Returns:
            tuple[int, str]: The HTTP status code and the response JSON.
        """
        token = self.kc_api.get_kc_token()
        if not token:
            return 500, {"error": "Unable to obtain Keycloak token."}

        try:
            response = requests.post(
                f"{self.iapi_endpoint}/emails/api-errors",
                headers={"Authorization": f"Bearer {token['access_token']}"},
                json={
                    "error_message": error_message,
                    "stack_trace": stack_trace,
                    "timestamp": timestamp,
                    "logs_link": logs_link,
                },
                timeout=10,
            )
        except requests.RequestException as exc:
            return self._handle_request_exception(exc)
        return self._build_response(response)
