import api_environment

def test_process_count_msg_types():
    assert api_environment.process_count_msg_types("bsm, ssm, spat") == [
        "BSM",
        "SSM",
        "SPAT",
    ]
    assert api_environment.process_count_msg_types("  bsm , ssm ,spat ") == [
        "BSM",
        "SSM",
        "SPAT",
    ]
    assert api_environment.process_count_msg_types("bsm,,ssm,,spat") == [
        "BSM",
        "SSM",
        "SPAT",
    ]
    assert api_environment.process_count_msg_types("BSM,SSM,SPAT") == [
        "BSM",
        "SSM",
        "SPAT",
    ]
    assert api_environment.process_count_msg_types("bSm,sSm,sPat") == [
        "BSM",
        "SSM",
        "SPAT",
    ]
    assert api_environment.process_count_msg_types("bsm") == ["BSM"]
    assert api_environment.process_count_msg_types("") == []
    assert api_environment.process_count_msg_types("   ") == []
