import re

# check whether it is banck message or not
class MessageUtil:
    def isBankSms(self,message):
        words_to_search=['spent','card','bank']
        pattern = r'\b(?:' + '|'.join(words_to_search) + r')\b'
        return bool(re.search(pattern, message, flags=re.IGNORECASE))
