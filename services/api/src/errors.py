from flask import jsonify


class BadRequestException(Exception):
    pass


class UnauthorizedException(Exception):
    pass


class NotFoundException(Exception):
    pass


class ServerErrorException(Exception):
    pass


class ServiceUnavailableException(Exception):
    pass


def register_error_handlers(app):
    # Catch Schema load errors
    @app.errorhandler(BadRequestException)
    def bad_request_error(error):
        return jsonify({"error": f"Bad Request: {str(error)}"}), 400

    @app.errorhandler(UnauthorizedException)
    def unauthorized_error(error):
        return jsonify({"message": f"Unauthorized: {str(error)}"}), 403

    @app.errorhandler(NotFoundException)
    def not_found_error(error):
        return jsonify({"message": f"Not Found: {str(error)}"}), 404

    @app.errorhandler(ServerErrorException)
    def internal_server_error(error):
        return jsonify({"message": f"Internal Server Error: {str(error)}"}), 500

    @app.errorhandler(ServiceUnavailableException)
    def service_unavailable_error(error):
        return jsonify({"message": f"Internal Server Error: {str(error)}"}), 503
