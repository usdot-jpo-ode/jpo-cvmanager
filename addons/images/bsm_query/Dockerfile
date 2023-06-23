FROM python:3-slim

WORKDIR /home

ADD requirements.txt .

RUN pip3 install -r requirements.txt

ADD bsm_query.py .

CMD ["/home/bsm_query.py"]
ENTRYPOINT ["python3"]