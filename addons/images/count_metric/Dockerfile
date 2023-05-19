FROM gcr.io/google-appengine/python

RUN pip3 install futures
RUN pip3 install datetime
RUN pip3 install requests
RUN pip3 install google-auth
RUN pip3 install kafka-python
RUN pip3 install APScheduler
RUN pip3 install --upgrade google-cloud-pubsub
RUN pip3 install --upgrade google-cloud-bigquery

ADD driver.py /home/driver.py
ADD kafka_counter.py /home/kafka_counter.py
ADD pubsub_counter.py /home/pubsub_counter.py

CMD ["/home/driver.py"]
ENTRYPOINT ["python3"]