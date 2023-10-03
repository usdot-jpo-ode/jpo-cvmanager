FROM python:3.11.3-alpine3.18

ADD crontab /home/pingfetch/
ADD rsuStatusFetch.py /home/pingfetch/
ADD pgquery_rsu.py /home/pingfetch/
ADD purger.py /home/pingfetch/
ADD requirements.txt .

RUN pip3 install -r requirements.txt
# fix the line endings from windows
RUN dos2unix /home/pingfetch/crontab

RUN crontab /home/pingfetch/crontab

CMD ["crond", "-f"]