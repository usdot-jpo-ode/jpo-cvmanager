import os
from datetime import datetime, timedelta
from mock import MagicMock, patch
from addons.images.count_metric import gen_email

message_types = ["BSM", "TIM", "Map", "SPaT", "SRM", "SSM"]


def test_diff_to_color():
    result = gen_email.diff_to_color(2)
    assert result == "#a4ffa1"

    result = gen_email.diff_to_color(7)
    assert result == "#ff7373"


def test_generate_table_header():
    result = gen_email.generate_table_header(message_types)

    expected = (
        "<thead>\n"
        '<tr style="text-align: center;background-color: #b0dfff;">\n'
        '<th style="padding: 12px;">RSU</th>\n'
        '<th style="padding: 12px;">Road</th>\n'
        '<th style="padding: 12px;">BSM In</th>\n'
        '<th style="padding: 12px;">BSM Out</th>\n'
        '<th style="padding: 12px;">TIM In</th>\n'
        '<th style="padding: 12px;">TIM Out</th>\n'
        '<th style="padding: 12px;">Map In</th>\n'
        '<th style="padding: 12px;">Map Out</th>\n'
        '<th style="padding: 12px;">SPaT In</th>\n'
        '<th style="padding: 12px;">SPaT Out</th>\n'
        '<th style="padding: 12px;">SRM In</th>\n'
        '<th style="padding: 12px;">SRM Out</th>\n'
        '<th style="padding: 12px;">SSM In</th>\n'
        '<th style="padding: 12px;">SSM Out</th>\n'
        "</tr>\n</thead>\n"
    )

    assert result == expected


def test_generate_table_row():
    rsu_ip = "10.0.0.1"
    data = {
        "primary_route": "Route 1",
        "counts": {
            "BSM": {"in": 0, "out": 0, "diff_percent": 0},
            "TIM": {"in": 1, "out": 1, "diff_percent": 0},
            "Map": {"in": 2, "out": 2, "diff_percent": 0},
            "SPaT": {"in": 0, "out": 0, "diff_percent": 0},
            "SRM": {"in": 2, "out": 2, "diff_percent": 0},
            "SSM": {"in": 0, "out": 0, "diff_percent": 0},
        },
    }
    row_style = "text-align: center;"

    result = gen_email.generate_table_row(rsu_ip, data, row_style, message_types)

    expected = (
        '<tr style="text-align: center;">\n'
        "<td>10.0.0.1</td>\n"
        "<td>Route 1</td>\n"
        "<td>0</td>\n"
        '<td style="background-color: #a4ffa1;">0</td>\n'
        "<td>1</td>\n"
        '<td style="background-color: #a4ffa1;">1</td>\n'
        "<td>2</td>\n"
        '<td style="background-color: #a4ffa1;">2</td>\n'
        "<td>0</td>\n"
        '<td style="background-color: #a4ffa1;">0</td>\n'
        "<td>2</td>\n"
        '<td style="background-color: #a4ffa1;">2</td>\n'
        "<td>0</td>\n"
        '<td style="background-color: #a4ffa1;">0</td>\n'
        "</tr>\n"
    )

    assert result == expected


@patch("addons.images.count_metric.gen_email.generate_table_header")
@patch("addons.images.count_metric.gen_email.generate_table_row")
def test_generate_count_table(mock_gen_table_row, mock_gen_table_header):
    mock_gen_table_header.return_value = ""
    mock_gen_table_row.return_value = ""
    rsu_dict = {
        "10.0.0.1": {
            "primary_route": "Route 1",
            "counts": {
                "BSM": {"in": 0, "out": 0, "diff_percent": 0},
                "TIM": {"in": 1, "out": 1, "diff_percent": 0},
                "Map": {"in": 2, "out": 2, "diff_percent": 0},
                "SPaT": {"in": 0, "out": 0, "diff_percent": 0},
                "SRM": {"in": 2, "out": 2, "diff_percent": 0},
                "SSM": {"in": 0, "out": 0, "diff_percent": 0},
            },
        }
    }

    result = gen_email.generate_count_table(rsu_dict, message_types)

    expected = '<table class="dataframe">\n<tbody>\n</tbody>\n</table>'

    assert result == expected


def test_generate_count_table_empty():
    rsu_dict = {}

    result = gen_email.generate_count_table(rsu_dict, message_types)

    assert result == ""


@patch.dict(
    os.environ,
    {"DEPLOYMENT_TITLE": "Test"},
)
@patch("addons.images.count_metric.gen_email.generate_count_table")
def test_generate_email_body(mock_generate_count_table):
    mock_generate_count_table.return_value = ""
    rsu_dict = {
        "10.0.0.1": {
            "primary_route": "Route 1",
            "counts": {
                "BSM": {"in": 0, "out": 0, "diff_percent": 0},
                "TIM": {"in": 1, "out": 1, "diff_percent": 0},
                "Map": {"in": 2, "out": 2, "diff_percent": 0},
                "SPaT": {"in": 0, "out": 0, "diff_percent": 0},
                "SRM": {"in": 2, "out": 2, "diff_percent": 0},
                "SSM": {"in": 0, "out": 0, "diff_percent": 0},
            },
        }
    }
    start_dt = (datetime.now() - timedelta(1)).replace(
        hour=0, minute=0, second=0, microsecond=0
    )
    end_dt = (datetime.now()).replace(hour=0, minute=0, second=0, microsecond=0)

    result = gen_email.generate_email_body(rsu_dict, start_dt, end_dt, message_types)

    expected_start_string = datetime.strftime(start_dt, "%Y-%m-%d 00:00:00")
    expected_end_string = datetime.strftime(end_dt, "%Y-%m-%d 00:00:00")
    expected = (
        f"<h2>TEST Count Report {expected_start_string} UTC - {expected_end_string} UTC</h2>"
        "<p>This is an automated email to report yesterday's ODE message counts for J2735 messages going in and out of the ODE. "
        "In counts are the number of encoded messages received by the ODE from the load balancer. "
        "Out counts are the number of decoded messages that have come out of the ODE in JSON form and "
        "are available for querying in mongoDB. Ideally, these two counts should be identical. "
        "Although, some deviation is expected due to count recording timings. Outbound counts exceeding "
        "5% deviation with their corresponding inbound counts will be marked red. Outbound counts within the 5% deviation will be marked "
        "green. Map and TIM Out counts are deduplicated so these are going to be lower at 1 per hour. The deviation is normalized with this in mind. "
        'Any RSUs with a road name of "Unknown" are not recorded in the PostgreSQL database and might need to be added.</p>'
        "<h3>RSU Message Counts</h3>"
    )

    assert result == expected
