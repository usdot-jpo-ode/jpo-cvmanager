import pytest
from addons.images.iss_health_check import iss_health_check_environment


def test_get_storage_type_gcp():
    actual_value = iss_health_check_environment.process_storage_type("gcp", "postgres")
    assert actual_value == "gcp"


def test_get_storage_type_postgres():
    actual_value = iss_health_check_environment.process_storage_type(
        "postgres", "postgres"
    )
    assert actual_value == "postgres"


def test_get_storage_type_gcp_case_insensitive():
    actual_value = iss_health_check_environment.process_storage_type("GCP", "postgres")
    assert actual_value == "gcp"


def test_get_storage_type_postgres_case_insensitive():
    actual_value = iss_health_check_environment.process_storage_type(
        "POSTGRES", "postgres"
    )
    assert actual_value == "postgres"


def test_get_storage_type_invalid():
    with pytest.raises(ValueError):
        iss_health_check_environment.process_storage_type("test", "postgres")


def test_get_storage_type_unset():
    iss_health_check_environment.process_storage_type(None, "postgres") == "postgres"
