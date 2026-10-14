<?php
declare(strict_types=1);

namespace FlipScanner\Services;

use FlipScanner\Config;

/**
 * eBay Browse API client for active listing comparables.
 *
 * v1 scope: active listings only. Sold-price data (Marketplace Insights)
 * requires restricted access and is out of scope for v1.
 *
 * Setup: create an app at developer.ebay.com, then set EBAY_APP_ID
 * (client ID) and EBAY_CERT_ID (client secret) in .env.
 */
class EbayService
{
    private string $appId;
    private string $certId;
    private string $environment;
    private ?string $token = null;
    private int $tokenExpiresAt = 0;

    public function __construct()
    {
        $this->appId = Config::get('EBAY_APP_ID', '');
        $this->certId = Config::get('EBAY_CERT_ID', '');
        $this->environment = Config::get('EBAY_ENV', 'production'); // or 'sandbox'
    }

    private function baseUrl(): string
    {
        return $this->environment === 'sandbox'
            ? 'https://api.sandbox.ebay.com'
            : 'https://api.ebay.com';
    }

    /**
     * Get an application OAuth token (client credentials grant).
     * Cached in memory for the request lifetime.
     */
    private function getToken(): string
    {
        if ($this->token !== null && time() < $this->tokenExpiresAt - 60) {
            return $this->token;
        }

        if ($this->appId === '' || $this->certId === '') {
            throw new \RuntimeException('EBAY_APP_ID / EBAY_CERT_ID are not configured.');
        }

        $credentials = base64_encode($this->appId . ':' . $this->certId);

        $ch = curl_init($this->baseUrl() . '/identity/v1/oauth2/token');
        curl_setopt_array($ch, [
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_POST => true,
            CURLOPT_HTTPHEADER => [
                'Content-Type: application/x-www-form-urlencoded',
                'Authorization: Basic ' . $credentials,
            ],
            CURLOPT_POSTFIELDS => http_build_query([
                'grant_type' => 'client_credentials',
                'scope' => 'https://api.ebay.com/oauth/api_scope',
            ]),
            CURLOPT_TIMEOUT => 15,
        ]);

        $response = curl_exec($ch);
        $httpCode = (int) curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        if ($httpCode !== 200 || $response === false) {
            throw new \RuntimeException('eBay OAuth failed (HTTP ' . $httpCode . ').');
        }

        $data = json_decode((string) $response, true);
        $this->token = (string) ($data['access_token'] ?? '');
        $this->tokenExpiresAt = time() + (int) ($data['expires_in'] ?? 7200);

        if ($this->token === '') {
            throw new \RuntimeException('eBay OAuth returned no token.');
        }

        return $this->token;
    }

    /**
     * Search active listings.
     *
     * @return array<int, array{title: string, price: float, currency: string, url: string, image: string, condition: string}>
     */
    public function searchActiveListings(string $query, int $limit = 10): array
    {
        $token = $this->getToken();

        $params = http_build_query([
            'q' => $query,
            'limit' => max(1, min(50, $limit)),
            'filter' => 'buyingOptions:{FIXED_PRICE}',
            'sort' => 'price',
        ]);

        $ch = curl_init($this->baseUrl() . '/buy/browse/v1/item_summary/search?' . $params);
        curl_setopt_array($ch, [
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_HTTPHEADER => [
                'Authorization: Bearer ' . $token,
                'X-EBAY-C-MARKETPLACE-ID: EBAY_US',
                'Content-Type: application/json',
            ],
            CURLOPT_TIMEOUT => 15,
        ]);

        $response = curl_exec($ch);
        $httpCode = (int) curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        if ($httpCode !== 200 || $response === false) {
            throw new \RuntimeException('eBay search failed (HTTP ' . $httpCode . ').');
        }

        $data = json_decode((string) $response, true);
        $items = $data['itemSummaries'] ?? [];

        $results = [];
        foreach ($items as $item) {
            $results[] = [
                'title' => (string) ($item['title'] ?? ''),
                'price' => (float) ($item['price']['value'] ?? 0),
                'currency' => (string) ($item['price']['currency'] ?? 'USD'),
                'url' => (string) ($item['itemWebUrl'] ?? ''),
                'image' => (string) ($item['thumbnailImages'][0]['imageUrl'] ?? ''),
                'condition' => (string) ($item['condition'] ?? ''),
            ];
        }

        return $results;
    }

    /**
     * Compute low/typical/high estimate from active listing prices.
     * Uses percentiles to reduce outlier influence.
     *
     * @param float[] $prices
     * @return array{low: float, typical: float, high: float}
     */
    public static function priceRange(array $prices): array
    {
        $prices = array_values(array_filter($prices, fn($p) => $p > 0));
        if (count($prices) === 0) {
            return ['low' => 0.0, 'typical' => 0.0, 'high' => 0.0];
        }

        sort($prices);
        $n = count($prices);

        $percentile = function (float $p) use ($prices, $n): float {
            $idx = ($n - 1) * $p;
            $lo = (int) floor($idx);
            $hi = (int) ceil($idx);
            if ($lo === $hi) {
                return $prices[$lo];
            }
            return $prices[$lo] + ($prices[$hi] - $prices[$lo]) * ($idx - $lo);
        };

        return [
            'low' => round($percentile(0.25), 2),
            'typical' => round($percentile(0.50), 2),
            'high' => round($percentile(0.75), 2),
        ];
    }
}
