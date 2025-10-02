from api.src import environment

def test_process_count_msg_types():
    assert environment.process_count_msg_types("bsm, ssm, spat") == ["BSM", "SSM", "SPAT"]
    assert environment.process_count_msg_types("  bsm , ssm ,spat ") == ["BSM", "SSM", "SPAT"]
    assert environment.process_count_msg_types("bsm,,ssm,,spat") == ["BSM", "SSM", "SPAT"]
    assert environment.process_count_msg_types("BSM,SSM,SPAT") == ["BSM", "SSM", "SPAT"]
    assert environment.process_count_msg_types("bSm,sSm,sPat") == ["BSM", "SSM", "SPAT"]
    assert environment.process_count_msg_types("bsm") == ["BSM"]
    assert environment.process_count_msg_types("") == []
    assert environment.process_count_msg_types("   ") == []