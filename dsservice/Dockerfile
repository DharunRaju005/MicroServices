FROM python:3.11.4
WORKDIR /app

# Copy the distribution package
COPY dist/ds_service-1.0.tar.gz .

# Install the distribution package
RUN pip install --no-cache-dir ds_service-1.0.tar.gz

# Set the environment variable for the Flask app
ENV FLASK_APP=src/app/__init__.py

ENV FLASK_ENV=development        
ENV FLASK_DEBUG=1  

# Expose the port
EXPOSE 8010

# Start the Flask app
CMD ["flask", "run", "--host=0.0.0.0", "--port=8010"]