FROM python:3.11.3-alpine3.18

ADD crontab /home/.
ADD requirements.txt /home/.
ADD *.py /home/.

RUN pip3 install -r /home/requirements.txt

RUN dos2unix /home/crontab

RUN crontab /home/crontab

CMD ["crond", "-f"]