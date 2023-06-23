FROM python:3-slim

WORKDIR /home

ADD requirements.txt .

RUN pip3 install -r requirements.txt

ADD driver.py .
ADD kafka_counter.py .
ADD pgquery_rsu.py .

CMD ["/home/driver.py"]
ENTRYPOINT ["python3"]