import binascii
import uuid
import pytz

from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.x509 import load_pem_x509_certificate


def create_access_token(cert_file, api_key, expiration_time):
    cert = load_pem_x509_certificate(cert_file.read(), default_backend())

    public_key = cert.public_key()

    try:
        expiration_time = pytz.utc.localize(expiration_time)
    except ValueError:
        pass

    expiration_string = expiration_time.isoformat()

    data_block = '{}#{}#{}'.format(api_key, uuid.uuid4(), expiration_string)

    ciphertext = public_key.encrypt(data_block, padding.PKCS1v15())

    return binascii.hexlify(ciphertext)


def main():
    import datetime
    import sys

    if len(sys.argv) != 4:
        print("Usage: {} cert_file api_key, expiration_timestamp".format(
            sys.argv[0]))
        exit(1)

    with open(sys.argv[1], "rb") as cert_file:
        expiration_time = datetime.datetime.fromtimestamp(int(sys.argv[3]))

        print(create_access_token(cert_file, sys.argv[2], expiration_time))


if __name__ == '__main__':
    main()