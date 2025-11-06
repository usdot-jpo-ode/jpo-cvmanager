from typing import Iterator
from unittest.mock import MagicMock, patch
import logging

import pytest

from common.auth_tools import ENVIRON_USER_KEY
from api.tests.data import auth_data

user_valid = auth_data.get_request_environ()


@pytest.fixture(scope="session", autouse=True)
def default_session_fixture() -> Iterator[None]:
    logging.info("Patching core.feature.service")
    with patch(
        "common.auth_tools.request",
        MagicMock(
            environ={ENVIRON_USER_KEY: user_valid},
        ),
    ):
        yield
    logging.info("Patching complete. Unpatching")
