from flask import Flask, jsonify, request
from subprocess import Popen, DEVNULL
from threading import Lock
from waitress import serve
import json
import logging
import os
