from flask import Flask
from flask import request,jsonify
# shd give absolute since we are dockerising
from .service.messageService import MessageService
from kafka import KafkaProducer
import json
import os

app = Flask(__name__)
app.config.from_pyfile('config.py')


messageService = MessageService()
kafka_host = os.getenv('KAFKA_HOST', 'localhost')
kafka_port = os.getenv('KAFKA_PORT', '9092')
kafka_bootstrap_servers = f"{kafka_host}:{kafka_port}"

producer=KafkaProducer(bootstrap_servers=kafka_bootstrap_servers,value_serializer=lambda v:json.dumps(v).encode('utf-8'))
@app.route('/v1/ds/message/', methods=['POST'])
def handle_message():
    user_id = request.headers.get('X-User-Id')
    print("User ID:", user_id)
    
    message = request.json.get('message')
    print("Received message:", message)
    
    result = messageService.process_message(message)
    print("Processed result:", result)
    
    if result is not None:
        serialized_result = result.serialize()
        serialized_result['user_id'] = user_id
        print("Serialized result:", serialized_result)
        producer.send('expense_service', serialized_result)
        return jsonify(serialized_result)
    else:
        return jsonify({'error': 'Invalid message format'}), 400

@app.route('/',methods=['GET'])
def handle_get():
    return "jeichuta maara"

if __name__=="__main__":
    app.run(host="localhost",port=8010,debug=True)