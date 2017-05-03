<?php

require('vendor/autoload.php');

class CreateAccessToken
{

    private $certificate;
    private $apiKey;
    private $expirationTime;

    public function __construct($certificate, $apiKey, DateTime $expirationTime)
    {
        $this->certificate = $certificate;
        $this->apiKey = $apiKey;
        $this->expirationTime = $expirationTime;
    }

    public function createToken()
    {
        $cert = openssl_get_publickey($this->certificate);

        $this->expirationTime->setTimeZone(new DateTimeZone('UTC'));

        $expirationAsISO8601 = $this->expirationTime->format(DateTime::ISO8601);
        $guid = $this->generateUUID();
        $dataBlock = sprintf('%s#%s#%s', $this->apiKey, $guid, $expirationAsISO8601);

        openssl_public_encrypt($dataBlock, $encryptedData, $cert);

        $encryptedDataHex = strtoupper(bin2hex($encryptedData));

        return $encryptedDataHex;
    }

    private function generateUUID()
    {
        return str_replace('-', '', Ramsey\Uuid\Uuid::uuid4());
    }

}
