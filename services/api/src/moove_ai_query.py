from google.cloud import bigquery
import os
import logging
import json
import pandas as pd
from shapely import wkt


def query_moove_ai(pointList):
    pointListWkt = (
        "POLYGON(("
        + ", ".join([f"{point[0]} {point[1]}" for point in pointList])
        + "))"
    )

    client = bigquery.Client(location="US", project=os.getenv("GCP_PROJECT_ID"))
    segment_agg_stats_table = os.getenv("MOOVE_AI_SEGMENT_AGG_STATS_TABLE")
    segment_event_stats_table = os.getenv("MOOVE_AI_SEGMENT_EVENT_STATS_TABLE")
    query = f"""
        SELECT 
            sas.segment_id, 
            sev.total_hard_brake_count, 
            sev.shape_geom  
        FROM 
            `{segment_agg_stats_table}` AS sas 
        INNER JOIN 
            `{segment_event_stats_table}` AS sev ON (sas.segment_id = sev.segment_id)
        WHERE ST_WITHIN(
            sev.shape_geom, 
            ST_GEOGFROMTEXT('{pointListWkt}')
        ) OR ST_INTERSECTS(
            sev.shape_geom, 
            ST_GEOGFROMTEXT('{pointListWkt}'))
        ORDER BY segment_id DESC
        """

    try:
        logging.debug(f"Starting query: {query}")
        df = client.query(query).to_dataframe()
        df = df.where(pd.notnull(df), None)

        segment_data = []
        for index, row in df.iterrows():
            shape_geom = wkt.loads(row["shape_geom"])
            segment_geojson = {
                "type": "Feature",
                "geometry": {
                    "type": "LineString",
                    "coordinates": list(shape_geom.coords),
                },
                "properties": {
                    "segment_id": row["segment_id"],
                    "total_hard_brake_count": row["total_hard_brake_count"],
                },
            }

            segment_data.append(segment_geojson)

        logging.info(f"Total Moove AI segments processed: {len(segment_data)}")
        return segment_data, 200
    except Exception as e:
        logging.error(f"Moove AI query failed: {e}")
        return [], 500


# REST endpoint resource class and schema
from flask import request
from flask_restful import Resource
from marshmallow import Schema, fields


class MooveAiDataSchema(Schema):
    geometry = fields.List(fields.List(fields.Float))


class MooveAiData(Resource):
    options_headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Access-Control-Allow-Headers": "Content-Type,Authorization",
        "Access-Control-Allow-Methods": "POST",
        "Access-Control-Max-Age": "3600",
    }

    headers = {
        "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
        "Content-Type": "application/json",
    }

    def options(self):
        # CORS support
        return ("", 204, self.options_headers)

    def post(self):
        logging.debug("MooveAiData POST requested")

        # Check for main body values
        schema = MooveAiDataSchema()
        errors = schema.validate(request.json)
        if errors:
            logging.error(str(errors))
            return (
                'Body format: {"geometry": coordinate list}',
                400,
                self.headers,
            )

        pointList = request.json["geometry"]
        data, code = query_moove_ai(pointList)

        return (data, code, self.headers)
