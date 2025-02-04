import logging
import os
from datetime import datetime


def diff_to_color(val):
    return "#ff7373" if val > 5 else "#a4ffa1"


def generate_table_header(message_type_list):
    html = (
        "<thead>\n"
        '<tr style="text-align: center;background-color: #b0dfff;">\n'
        '<th style="padding: 12px;">RSU</th>\n'
        '<th style="padding: 12px;">Road</th>\n'
    )

    for type in message_type_list:
        html += f'<th style="padding: 12px;">{type} In</th>\n'
        html += f'<th style="padding: 12px;">{type} Out</th>\n'

    html += "</tr>\n</thead>\n"
    return html


def generate_table_row(rsu_ip, data, row_style, message_type_list):
    html = (
        f'<tr style="{row_style}">\n'
        f"<td>{rsu_ip}</td>\n"
        f'<td>{data["primary_route"]}</td>\n'
    )

    for type in message_type_list:
        html += f'<td>{data["counts"][type]["in"]}</td>\n'
        html += f'<td style="background-color: {diff_to_color(data["counts"][type]["diff_percent"])};">{data["counts"][type]["out"]}</td>\n'

    html += "</tr>\n"
    return html


def generate_count_table(rsu_dict, message_type_list):
    logging.info(f"Creating count table...")

    # If the RSU dictionary is completely empty, return nothing to indicate an issue has occurred somewhere
    if not rsu_dict:
        logging.error("RSU dictionary is empty. Most likely an issue with PostgreSQL")
        return ""

    html = f'<table class="dataframe">\n{generate_table_header(message_type_list)}<tbody>\n'

    style_switch = False
    for rsu_ip, value in rsu_dict.items():
        row_style = (
            "text-align: center;background-color: #f2f2f2;"
            if style_switch
            else "text-align: center;"
        )
        style_switch = not style_switch

        # Calculate differences between In and Out counts (%)
        for type in value["counts"]:
            in_count = value["counts"][type]["in"]
            out_count = value["counts"][type]["out"]

            if type.lower() == "bsm" or type.lower() == "tim":
                # For unique deduplication situations, don't validate counts unless zero
                # Assign the percentage difference between the in and out counts as pass or
                # fail since no validation is occurring
                # 6 being 6% and 0 being 0% difference. The 6% is enough to flag the table cell value
                value["counts"][type]["diff_percent"] = (
                    6
                    if (in_count != 0 and out_count == 0) or (out_count > in_count)
                    else 0
                )
            else:
                # Normalize the diff_percent depending on message types that are deduplicated to 1/hour
                x = 3600 if type.lower() == "map" else 1
                # Assign the calculated percentage difference between the in and out counts
                # 6 (6%) is enough to flag a table cell value
                value["counts"][type]["diff_percent"] = (
                    abs(out_count / -(-(in_count / x) // 1) - 1) * 100
                    if in_count != 0
                    else (6 if out_count > in_count else 0)
                )

        html += generate_table_row(rsu_ip, value, row_style, message_type_list)

    html += "</tbody>\n</table>"

    return html


def generate_email_body(org_name, rsu_dict, start_dt, end_dt, message_type_list):
    start = datetime.strftime(start_dt, "%Y-%m-%d 00:00:00")
    end = datetime.strftime(end_dt, "%Y-%m-%d 00:00:00")

    # DEPLOYMENT_TITLE is a contextual title for where these counts apply. ie. "GCP prod"
    # This is generalized to support any deployment environment
    html = (
        f'<h2>{org_name} {str(os.environ["DEPLOYMENT_TITLE"])} Count Report {start} UTC - {end} UTC</h2>'
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

    table = generate_count_table(rsu_dict, message_type_list)
    html += table

    return html
