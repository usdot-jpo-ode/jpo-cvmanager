FROM python:3-slim

WORKDIR /home

ADD requirements.txt .

RUN pip3 install -r requirements.txt

ADD geo_msg_query.py .
ADD kafka_helper.py .

CMD ["/home/geo_msg_query.py"]
ENTRYPOINT ["python3"]