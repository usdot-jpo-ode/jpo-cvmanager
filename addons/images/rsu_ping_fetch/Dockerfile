FROM python:3.6.12-alpine3.12

ADD crontab /home/pingfetch/
ADD rsuStatusFetch.py /home/pingfetch/
ADD pgquery_rsu.py /home/pingfetch/

RUN pip install requests
RUN pip install google-auth
# fix the line endings from windows
RUN dos2unix /home/pingfetch/crontab

RUN crontab /home/pingfetch/crontab

CMD ["crond", "-f"]
